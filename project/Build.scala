import _root_.sbt.Keys._
import _root_.sbt._
import com.typesafe.sbt.SbtNativePackager.{Linux, Debian}
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
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
      appApi,
      crawlerSiteSearch,
      moduleSiteSearch, moduleNews,
      util)

  ///////////////////////////////////////////////////////////////
  // projects
  ///////////////////////////////////////////////////////////////
  lazy val packageDebianProd = taskKey[File]("creates deb-prod package")
  lazy val appApi = Project("app-api", file("app-api"))
    .enablePlugins(JavaServerAppPackaging)
    .dependsOn(moduleSiteSearch % DependsConfigure, moduleNews % DependsConfigure, util % DependsConfigure)
    .settings(basicSettings: _*)
    .settings(
      description := "app-api",

      packageDescription := "一个高级异步多线程实时爬虫API",
      mainClass in Compile := Some("crawler.app.Main"),
      maintainer in Linux := "Jing Yang <jing.yang@socialcredits.cn, yangbajing@gmail.com>",
      packageSummary in Linux := "Crawler High Search API",
      daemonUser in Linux := "nobody",
      bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts"),
      bashScriptExtraDefines += """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml"""",

      //          |; bashScriptExtraDefines := Seq("addJava \"-Dconfig.file=${app_home}/../conf/application.conf -Dlogback.configurationFile=${app_home}/../conf/logback.xml\"")
      addCommandAlias("packageProd",
        """; clean
          |; bashScriptExtraDefines += "addJava \"-Dconfig.file=${app_home}/../conf/application-test.conf -Dlogback.configurationFile=${app_home}/../conf/logback.xml\""
          |; packageDebianProd
        """.stripMargin),
      packageDebianProd := {
        bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application-test.conf -Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
        val output = baseDirectory.value / "package" / "deb-prod.deb"
        val debianFile = (packageBin in Debian).value
        IO.move(debianFile, output)
        output
      },

//      assemblyJarName in assembly := "crawler-app.jar",
//      mappings in Universal <<= (mappings in Universal, assembly in Compile) map { (mappings, fatJar) =>
//        val filtered = mappings filter { case (file, name) => !name.endsWith(".jar") }
//        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
//      },
//      test in assembly := {},
//      assemblyMergeStrategy in assembly := {
//        case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
//        case x =>
//          val oldStrategy = (assemblyMergeStrategy in assembly).value
//          oldStrategy(x)
//      },

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
