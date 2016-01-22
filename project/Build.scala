import _root_.sbt.Keys._
import _root_.sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}

object Build extends Build {

  import BuildSettings._

  val DependsConfigure = "test->test;compile->compile"

  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val root = Project("crawler-high-search", file("."))
    .aggregate(
      app,
      consumerSiteSearch,
      siteSearch, news,
      util)

  ///////////////////////////////////////////////////////////////
  // projects
  ///////////////////////////////////////////////////////////////
  lazy val app = Project("app", file("app"))
    .dependsOn(siteSearch % DependsConfigure, news % DependsConfigure, util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "app",
      assemblyJarName in assembly := "crawler-app.jar",
      mainClass in assembly := Some("crawler.app.Main"),
      test in assembly := {},
      assemblyMergeStrategy in assembly := {
        case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      },
      libraryDependencies ++= Seq(
        _akkaHttp)
    )

  lazy val consumerSiteSearch = Project("site-search-consumer", file("site-search-consumer"))
    .dependsOn(siteSearch % DependsConfigure, util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "site-search-consumer",
      libraryDependencies ++= Seq(
        _cassandraDriverCore,
        _mongoScala)
    )

  lazy val siteSearch = Project("site-search", file("site-search"))
    .dependsOn(util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "site-search"
    )

  lazy val news = Project("news", file("news"))
    .dependsOn(util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "news",
      libraryDependencies ++= Seq(
        _cassandraDriverCore,
        _akkaActor)
    )

  lazy val util = Project("util", file("util"))
    .settings(basicSettings: _*)
    .settings(
      description := "util",
      libraryDependencies ++= Seq(
        _cassandraDriverCore % "provided",
        _mongoScala % "provided",
        _akkaHttp % "provided",
        _akkaStream,
        _json4sJackson,
        _json4sExt,
        _scalaLogging,
        _asyncHttpClient,
        _jsoup,
        _akkaActor,
        _akkaSlf4j,
        _logbackClassic)
    )

}
