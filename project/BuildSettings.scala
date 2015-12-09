import sbt.Keys._
import sbt._

object BuildSettings {

  lazy val basicSettings = Seq(
    version := "0.0.1",
    homepage := Some(new URL("https://github.com/yangbajing/crawler-service")),
    organization := "me.yangbajing",
    organizationHomepage := Some(new URL("https://github.com/yangbajing/crawler-service")),
    startYear := Some(2015),
    scalaVersion := "2.11.7",
    scalacOptions := Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-feature",
      "-deprecation"
    ),
    javacOptions := Seq(
      "-encoding", "utf8",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),
    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"),
    libraryDependencies ++= Seq(
      _scalaReflect,
      _scalatest
    ),
    offline := true,
    fork := true
  )

  lazy val noPublishing = Seq(
    publish :=(),
    publishLocal :=()
  )

  val verAkka = "2.4.1"
  val _akkaActor = "com.typesafe.akka" %% "akka-actor" % verAkka
  val _akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % verAkka

  val verAkkaHttp = "2.0-M2"
  lazy val _scalaReflect =  "org.scala-lang" % "scala-reflect" % "2.11.7"

//  lazy val _akkaHttpCore = ("com.typesafe.akka" %% "akka-http-core-experimental" % verAkkaHttp).exclude("com.typesafe.akka", "akka-actor")
  lazy val _akkaHttp = ("com.typesafe.akka" %% "akka-http-experimental" % verAkkaHttp).exclude("com.typesafe.akka", "akka-actor")

  lazy val _scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"

  lazy val _scalaLogging = ("com.typesafe.scala-logging" %% "scala-logging" % "3.1.0").exclude("org.scala-lang", "scala-reflect").exclude("org.slf4j", "slf4j-api")

  lazy val _mongoScala = "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0-rc0"

  lazy val varJson4s = "3.3.0"
  lazy val _json4sJackson = "org.json4s" %% "json4s-jackson" % varJson4s
  lazy val _json4sExt = "org.json4s" %% "json4s-ext" % varJson4s

  lazy val _jsoup = "org.jsoup" % "jsoup" % "1.8.3"

  lazy val _asyncHttpClient = ("com.ning" % "async-http-client" % "1.9.31").exclude("io.netty", "netty")

  lazy val _logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"

  lazy val _cassandraDriverCore = "com.datastax.cassandra" % "cassandra-driver-core" % "2.2.0-rc3"

}

