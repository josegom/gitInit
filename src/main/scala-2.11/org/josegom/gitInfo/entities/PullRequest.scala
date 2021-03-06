package org.josegom.gitInfo.entities

/**
  * Created by jmgomez on 6/18/16.
  */
case class PullRequest(title: String, id:String, number:String, creationDate: String, closeDate:String, creator:Person, commentor: Seq[Person]) {
  def toHtml: String = s"<tr><td>${creator.name}</td><td>${id}</td><td>${number}</td>${commentor.distinct.map(_.toHtml).mkString("")}</tr>"
}

