package crawler.app

import java.nio.file.{Files, Paths}

import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import crawler.SystemUtils
import crawler.util.Utils

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Main
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object Main extends App {

  import SystemUtils._
  import system.dispatcher

  Files.write(Paths.get("app.pid"), Utils.getPid.getBytes(Utils.CHARSET))

  val config = ConfigFactory.load()

  Http().bindAndHandle(ApiRoutes(), config.getString("crawler.network.server"), config.getInt("crawler.network.port"))
    .onComplete {
      case Success(binding) =>
        println(s"binding: $binding")
      case Failure(e) =>
        e.printStackTrace()
        SystemUtils.shutdown()
    }
}
