import _root_.sbt.Keys._
import _root_.sbt._
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging

object Build extends Build {

  import BuildSettings._

  val DependsConfigure = "test->test;compile->compile"

  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val root = Project("crawler-high-search", file("."))
    .aggregate(
      appApi,
      crawlerSiteSearch,
      moduleSiteSearch, moduleNews,
      util)

  ///////////////////////////////////////////////////////////////
  // projects
  ///////////////////////////////////////////////////////////////
  lazy val appApi = Project("app-api", file("app-api"))
    .enablePlugins(JavaServerAppPackaging)
    .dependsOn(moduleSiteSearch % DependsConfigure, moduleNews % DependsConfigure, util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "app-api",
      //      assemblyJarName in assembly := "crawler-app.jar",
      //      test in assembly := {},
      //      assemblyMergeStrategy in assembly := {
      //        case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
      //        case x =>
      //          val oldStrategy = (assemblyMergeStrategy in assembly).value
      //          oldStrategy(x)
      //      },
      mainClass := Some("crawler.app.Main"),
      maintainer := "Jing Yang <jing.yang@socialcredits.cn, yangbajing@gmail.com>",
      packageSummary := "Crawler High Search API",
      packageDescription := "一个高级爬虫异步多线程实时API",
      libraryDependencies ++= Seq(
        _akkaHttp)
    )

  lazy val crawlerSiteSearch = Project("crawler-site-search", file("crawler-site-search"))
    .dependsOn(moduleSiteSearch % DependsConfigure, util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "crawler-site-search",
      libraryDependencies ++= Seq(
        _activemqSTOMP,
        _cassandraDriverCore,
        _mongoScala)
    )

  lazy val moduleSiteSearch = Project("module-site-search", file("module-site-search"))
    .dependsOn(util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "module-site-search"
    )

  lazy val moduleNews = Project("module-news", file("module-news"))
    .dependsOn(util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "module-news",
      libraryDependencies ++= Seq(
        _cassandraDriverCore,
        _akkaActor)
    )

  lazy val util = Project("util", file("util"))
    .settings(basicSettings: _*)
    .settings(
      description := "util",
      libraryDependencies ++= Seq(
        _activemqSTOMP % "provided",
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
