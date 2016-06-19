package org.josegom.gitInfo

import org.josegom.gitInfo.entities.Project

/**
  * Created by jmgomez on 6/19/16.
  */
class Calculator {



  def calculateprMean(reviewer: String, p: Project): Unit = {
    p.pr.foreach(
      pr => pr.get.
    )
  }

  def reviewMean(reviewer: String, projects:  Seq[Project]): Unit = {
    projects.foreach(p => {
    println(p.name)
    calculateprMean(reviewer,p)
    }
  }

}
