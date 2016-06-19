package org.josegom.gitInfo.entities

/**
  * Created by jmgomez on 6/18/16.
  */
case class Project (name:String, pr: Seq[PullRequest]){
  def toHtml : String =  s"<tr><th>${name}</th></tr>${pr.map(_.toHtml).mkString("")}"
}

