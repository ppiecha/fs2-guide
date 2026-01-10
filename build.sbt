ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "FS2 Guide"
  )

libraryDependencies += "co.fs2" %% "fs2-core" % "3.12.2"
libraryDependencies += "co.fs2" %% "fs2-io" % "3.12.2"
