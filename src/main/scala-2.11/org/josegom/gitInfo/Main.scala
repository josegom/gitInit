package org.josegom.gitInfo

/**
  * Created by josegom on 6/18/16.
  */


import akka.event.Logging
import akka.io.IO
import akka.pattern.ask
import org.josegom.gitInfo.entities.{Person, Project, PullRequest}
import spray.can.Http
import spray.util._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http._
import org.json4s.JsonDSL._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.jackson.Serialization.{read, write}


object Main extends App {

  import system.dispatcher
  import collection.JavaConverters._


  if (args.length < 3) System.exit(1)
  val username = args(0)
  val password = args(1)
  val repositories: Seq[String] = args(2).split(",")

  println(s"${username} ${password} ${repositories}")

  implicit val formats = DefaultFormats
  implicit val system = akka.actor.ActorSystem("system")
  // execution context for futures


  println("start")
  val solution = repositories.map(project => Project(project, createPullRequest(project)))
  println("-------------------------------------------")
  println(write(solution))
  println(solution.map(_.toHtml))
  shutdown()

  def createPullRequest(project: String): Seq[PullRequest] = {
    val projectURl: String = s"https://api.github.com/repos/stratio/${project}/pulls?state=closed"
    println(s"Recover project: ${projectURl}")
    val pullRequest = Await.result[HttpResponse](executeRest(projectURl), 120 second)
    val allPr: Seq[PRDTO] = parse(pullRequest.entity.data.asString).extract[Seq[PRDTO]]
    allPr.map(pr =>
      PullRequest(pr.head.repo.created_at, pr.head.repo.updated_at, Person(pr.head.repo.owner.login), createCommends(pr))
    )
  }

  def executeRest(url: String): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (addCredentials(BasicHttpCredentials(username, password)) ~>
      sendReceive
      )
    pipeline(Get(url))
  }

  def shutdown(): Unit = {
    println("shutdown")
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }


  def createCommends(pr: PRDTO): Seq[Person] = {
    val coments = Await.result[HttpResponse](executeRest(pr.review_comments_url), 120 second)
    val allComents = parse(coments.entity.data.asString).extract[Seq[CommentDTO]]
    allComents.map(
      comment => Person(comment.user.login)
    )

  }
}


case class PRDTO(url: String, title: String, review_comments_url: String, head: HeadDto)

case class HeadDto(repo: RepoDTO)

case class RepoDTO(owner: UserDTO, name: String, created_at: String, updated_at: String)

case class UserDTO(login: String)

case class CommentDTO(user: UserDTO, created_at: String)