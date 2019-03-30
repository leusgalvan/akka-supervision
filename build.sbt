scalaVersion := "2.12.7"
organization := "com.example"

lazy val `supervision-strategies` = (project in file("."))
    .settings(
        name := "Supervision strategies"
    )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % Test
)
