package crawler.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import crawler.news.routes.ApiRoute
import crawler.news.service.NewsService

/**
 * Main
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val config = ConfigFactory.load()
  val newsService = new NewsService
  Http().bindAndHandle(ApiRoute(newsService), config.getString("crawler.news.network.server"), config.getInt("crawler.news.network.port"))
}
