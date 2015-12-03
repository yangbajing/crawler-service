import _root_.sbt.Keys._
import _root_.sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}

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
  lazy val app = Project("app", file("app"))
    .dependsOn(news, util)
    .settings(basicSettings: _*)
    .settings(
      description := "app",
      assemblyJarName in assembly := "crawler-app-news.jar",
      mainClass in assembly := Some("crawler.app.Main"),
      assemblyMergeStrategy in assembly := {
        case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      },
      libraryDependencies ++= Seq(
        _akkaHttp
      ))

  lazy val news = Project("news", file("news"))
    .dependsOn(util)
    .settings(basicSettings: _*)
    .settings(
      description := "news",
      libraryDependencies ++= Seq(
        _akkaHttp
      ))

  lazy val util = Project("util", file("util"))
    .settings(basicSettings: _*)
    .settings(
      description := "util",
      libraryDependencies ++= Seq(
        _akkaHttp % "provided",
        _json4sJackson,
        _json4sExt,
        //        _mongoScala,
        _cassandraDriverCore,
        _scalaLogging,
        _asyncHttpClient,
        _jsoup,
        _akkaActor,
        _akkaSlf4j,
        _logbackClassic
      ))
}
