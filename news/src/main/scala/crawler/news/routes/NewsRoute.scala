package crawler.news.routes

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.StrictLogging
import crawler.SystemUtils
import crawler.news.crawlers._
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.service.NewsService

import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * 新闻路由
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object NewsRoute extends StrictLogging {

  import akka.http.scaladsl.server.Directives._
  import crawler.news.JsonSupport._

  val httpClient = SystemUtils.httpClient
  NewsCrawler.registerCrawler(NewsSource.baidu, new BaiduNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.sogou, new SogouNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.haosou, new HaosouNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.court, new CourtNews(httpClient))
  //  NewsCrawler.registerCrawler(NewsSource.wechat, new WechatNews(httpClient))

  val newsService = new NewsService()

  def apply() =
    (path("news") & get) {
      parameters(
        'company.as[String],
        'source.as[String] ? "",
        'method.as[String] ? "",
        'duration.as[Int] ? 15,
        'forcedLatest.as[String] ? "") { (company, source, method, duration, forcedLatest) =>

        val sources = NewsSource.withToNames(source)
        val mtd = Try(SearchMethod.withName(method)).getOrElse(SearchMethod.F)
        complete {
          newsService.fetchNews(company, sources, mtd, Duration(duration, TimeUnit.SECONDS), forcedLatest == "y")
        }
      }
    }

}
