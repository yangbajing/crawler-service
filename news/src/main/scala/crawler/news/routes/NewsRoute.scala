package crawler.news.routes

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging
import crawler.news.service.NewsService
import crawler.news.{NewsSource, SearchMethod}
import crawler.util.http.JsonSupport._

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * 新闻路由
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object NewsRoute extends StrictLogging {

  val newsService = new NewsService

  def apply(pathname: String) = {
    path(pathname) {
      get {
        parameters(
          'company.as[String],
          'source.as[String] ? NewsSource.BAIDU.toString,
          'method.as[String] ? SearchMethod.F.toString,
          'duration.as[Int] ? 60) { (company, source, method, duration) =>

          val f = newsService.fetchNews(company, source.split(','), SearchMethod.withName(method),
            Duration(duration, TimeUnit.SECONDS))

          onComplete(f) {
            case Success(result) =>
              complete(result)

            case Failure(e) =>
              logger.error(e.getLocalizedMessage, e)
              complete(e.toString)
          }
        }
      }
    }
  }

}
