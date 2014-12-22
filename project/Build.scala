import sbt._
import Keys._


object ScalaTsxProject extends Build {
  lazy val root = Project(id = "scala-tsx-project",
    base = file(".")) aggregate (scalaTsx)

  lazy val scalaTsx = Project(id = "scala-tsx",
    base = file("scala-tsx"))

}
