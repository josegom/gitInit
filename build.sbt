lazy val root = (project in file(".")).
  settings(
    name := "gitInfo",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies += "io.spray" %% "spray-client" % "1.3.1",
    libraryDependencies ++= Seq("com.typesafe.akka" % "akka-actor_2.11" % "2.3.15",
        "org.http4s" % "http4s-json4s-jackson_2.11" % "0.14.1a")
  )

mainClass in (Compile,run) := Some("Main")
