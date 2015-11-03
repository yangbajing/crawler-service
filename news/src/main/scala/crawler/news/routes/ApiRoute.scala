package crawler.news.routes

import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import crawler.news.service.NewsService

import scala.concurrent.ExecutionContext

/**
 * Created by yangjing on 15-11-3.
 */
object ApiRoute {
  def apply(newsService: NewsService)(implicit ec: ExecutionContext, mat: Materializer) = {
    pathPrefix("api") {
      NewsRoute("news")
    }
  }
}
