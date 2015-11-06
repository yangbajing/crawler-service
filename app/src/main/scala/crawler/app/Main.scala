package crawler.app

import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import crawler.SystemUtils
import crawler.routes.ApiRoutes

/**
 * Main
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object Main extends App {

  import SystemUtils._

  val config = ConfigFactory.load()

  Http().bindAndHandle(ApiRoutes("api"), config.getString("crawler.network.server"), config.getInt("crawler.network.port"))
}
