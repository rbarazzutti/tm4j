import sbt._
import Keys._


object Dependencies {
  def funsuite = libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0-M1" % "test,compile"

  def junit = libraryDependencies += "junit" % "junit" % "4.12" % "test,compile"
}

object Tm4jProject extends Build {

  import Dependencies._

  def standardSettings = Defaults.coreDefaultSettings ++ Seq(
    scalacOptions ++= Seq("-target:jvm-1.6", "-feature"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in doc := Seq("-source", "1.8"),
    fork := true,
    parallelExecution in Test := false,
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked"),
    organization := "org.tm4j"
  )

  val noScala = Seq(
    autoScalaLibrary := false
  )

  lazy val root = Project(id = "tm4j-project",
    base = file(".")) aggregate(tm4j, tm4s, tm4jTsx)


  lazy val tm4j = Project(id = "tm4j",
    base = file("tm4j"),
    settings = standardSettings ++ noScala
  )

  lazy val tm4s = Project(id = "tm4s",
    base = file("tm4s"),
    settings = standardSettings ++ funsuite

  ).dependsOn(tm4j, tm4jDumb % "test")

  lazy val tm4jTsx = Project(id = "tm4j-tsx",
    base = file("tm4j-tsx"),
    settings = standardSettings ++ noScala
  ).dependsOn(tm4j)

  lazy val tm4jDumb = Project(id = "tm4j-dumb",
    base = file("tm4j-dumb"),
    settings = standardSettings ++ noScala ++ junit
  ).dependsOn(tm4j % "compile")


}
