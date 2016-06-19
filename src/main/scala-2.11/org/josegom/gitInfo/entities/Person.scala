package org.josegom.gitInfo.entities

/**
  * Created by jmgomez on 6/18/16.
  */
case class Person(name: String) {
  def toHtml: String = s"<td>${name}</td>"
}
