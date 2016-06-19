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
  //val repositories: Seq[String] = args(2).split(",")
  val repositories: Seq[String] = Seq("Gosec-SSO")
  println(s"${username} ${password} ${repositories}")

  implicit val formats = DefaultFormats
  implicit val system = akka.actor.ActorSystem("system")
  // execution context for futures


  println("start")
  val solution: Seq[Project] = repositories.map(project => Project(project, createPullRequest(project)))
  val groupBy: Seq[Map[Person, Seq[Option[PullRequest]]]] = solution.map(p => p.pr.groupBy(_.get.creator))
  println("-------------------------------------------")
  println(write(solution))
  println(groupBy.map(x => s"<tr>${x.map(y => s"<td>${y._1.name}</td>${y._2.map(pr => if (pr.isDefined) s"${pr.get.toHtml}").mkString("")}").mkString("")}</tr>").mkString(""))
  println(solution.map(_.toHtml))
  shutdown()

  def createPullRequest(project: String): Seq[Option[PullRequest]] = {
    val projectURl: String = s"https://api.github.com/repos/stratio/${project}/pulls?state=closed&per_page=110"
    val pullRequest = Await.result[HttpResponse](executeRest(projectURl), 5 minute)
    val allPr: Seq[PRDTO] = parse(pullRequest.entity.data.asString).extract[Seq[PRDTO]]
    allPr.map(pr => {
      if (pr.head != null && pr.head.repo != null && pr.head.repo.owner != null)
        Some(PullRequest(pr.title, pr.id, pr.number, pr.head.repo.created_at, pr.head.repo.updated_at, Person(pr.head.repo.owner.login), createCommends(pr)))
      else None
    }
    )
  }

  def executeRest(url: String): Future[HttpResponse] = {
    println(s"URL: $url")
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
    val review_coments = Await.result[HttpResponse](executeRest(s"${pr.review_comments_url}"), 5 minute)
    val comments = Await.result[HttpResponse](executeRest(s"${pr.comments_url}"), 5 minute)
    val allComents = parse(review_coments.entity.data.asString).extract[Seq[CommentDTO]] ++ parse(comments.entity.data.asString).extract[Seq[CommentDTO]]
    allComents.map(
      comment => Person(comment.user.login)
    )

  }
}


case class PRDTO(url: String, title: String, review_comments_url: String, comments_url: String, head: HeadDto, id: String, number: String)

case class HeadDto(repo: RepoDTO)

case class RepoDTO(owner: UserDTO, name: String, created_at: String, updated_at: String)

case class UserDTO(login: String)

case class CommentDTO(user: UserDTO, created_at: String)