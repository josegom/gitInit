package org.josegom.gitInfo.entities

/**
  * Created by jmgomez on 6/18/16.
  */
case class Project (name:String, pr: Seq[Option[PullRequest]]){
  def toHtml : String =  s"<tr><th>${name}</th></tr>${pr.map(pr => if (pr.isDefined) pr.get.toHtml).mkString("")}"
}

