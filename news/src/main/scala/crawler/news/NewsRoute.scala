package crawler.news

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import crawler.SystemUtils
import crawler.news.crawlers._
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.service.NewsService
import crawler.util.Utils

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * 新闻路由
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object NewsRoute extends StrictLogging {

  import SystemUtils.system.dispatcher
  import akka.http.scaladsl.server.Directives._
  import crawler.news.JsonSupport._

  val config = ConfigFactory.load()
  val httpClient = SystemUtils.httpClient
  NewsCrawler.registerCrawler(NewsSource.baidu, new BaiduNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.sogou, new SogouNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.haosou, new HaosouNews(httpClient))
  NewsCrawler.registerCrawler(NewsSource.court, new CourtNews(httpClient))
  //  NewsCrawler.registerCrawler(NewsSource.wechat, new WechatNews(httpClient))

  val newsService = new NewsService()

  def apply() =
    pathPrefix("news") {
      pathEnd {
        get {
          parameters(
            'company.as[String],
            'source.as[String] ? "",
            'method.as[String] ? "",
            'duration.as[Int] ? 15,
            'forcedLatest.as[String] ? "",
            'version.as[String] ? "1") { (company, source, method, duration, forcedLatest, version) =>

            val future: Future[HttpResponse] =
              version match {
                case "3" =>
                  fromLocal(company, Seq(NewsSource.baidu)/*NewsSource.withToNames(source)*/, method, duration, forcedLatest).flatMap(list =>
                    Marshal(list.flatMap(_.news)).to[HttpResponse]
                  )

                case "2" =>
                  fromCrawlerApi(company).recoverWith {
                    case e: Exception =>
                      logger.warn("fromCralwerApi recover with: " + e, e)
                      fromLocal(company, Seq(NewsSource.baidu), method, duration, forcedLatest).flatMap(list =>
                        Marshal(list.flatMap(_.news)).to[HttpResponse]
                      )
                  }

                case _ =>
                  fromLocal(company, Seq(NewsSource.baidu), method, duration, forcedLatest).flatMap(list =>
                    Marshal(list).to[HttpResponse]
                  )
              }
            complete(future)
          }
        }
      }
    }

  private def fromLocal(company: String, sources: Traversable[NewsSource.Value], method: String, duration: Int, forcedLatest: String) = {
    val mtd = Try(SearchMethod.withName(method)).getOrElse(SearchMethod.F)
    newsService.
      fetchNews(company, sources, mtd, Duration(duration, TimeUnit.SECONDS), forcedLatest == "y")
  }

  private def fromCrawlerApi(company: String) =
    httpClient.get(config.getString("crawler.api-uri") + "/api/news")
      .queryParam("companyName" -> company)
      .execute()
      .map { resp =>
        if (resp.getStatusCode != 200)
          throw new RuntimeException(s"crawler-api not found company: $company, return: ${resp.getStatusCode}")

        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-8`),
              resp.getResponseBody(Utils.CHARSET.name()))
        )
      }

}
