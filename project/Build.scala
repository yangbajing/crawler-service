import _root_.sbt.Keys._
import _root_.sbt._
import sbtassembly.AssemblyKeys._

object Build extends Build {

  import BuildSettings._

  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val root = Project("crawler-service", file("."))
    .aggregate(news)

  ///////////////////////////////////////////////////////////////
  // projects
  ///////////////////////////////////////////////////////////////
  lazy val news = Project("news", file("news"))
    .settings(basicSettings: _*)
    .settings(
      description := "news",
      assemblyJarName in assembly := "crawler-news.jar",
      mainClass in assembly := Some("crawler.news.Main"),
      libraryDependencies ++= Seq(
        _json4sJackson,
        _akkaHttpJson4s,
        _akkaHttp,
        _akkaActor,
        _scalaLogging,
        _logbackClassic
      ))

}
