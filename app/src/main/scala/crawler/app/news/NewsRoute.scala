package crawler.app.news

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import com.typesafe.config.ConfigFactory
import crawler.SystemUtils
import crawler.app.news.crawlers._
import crawler.app.news.service.NewsService
import crawler.common.BaseRoute
import crawler.enums.{ItemSource, SearchMethod}
import crawler.util.Utils

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * 新闻路由
  * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
  */
object NewsRoute extends BaseRoute {

  val config = ConfigFactory.load()
  NewsCrawler.registerCrawler(ItemSource.baidu, new BaiduNews(SystemUtils.httpClient))
  NewsCrawler.registerCrawler(ItemSource.sogou, new SogouNews(SystemUtils.httpClient))
  NewsCrawler.registerCrawler(ItemSource.haosou, new HaosouNews(SystemUtils.httpClient))
  NewsCrawler.registerCrawler(ItemSource.court, new CourtNews(SystemUtils.httpClient))
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
                  fromLocal(company, Seq(ItemSource.baidu) /*NewsSource.withToNames(source)*/ , method, duration, forcedLatest).flatMap(list =>
                    Marshal(list.flatMap(_.news)).to[HttpResponse]
                  )

                case "2" =>
                  fromCrawlerApi(company).recoverWith {
                    case e: Exception =>
                      logger.warn("fromCralwerApi recover with: " + e, e)
                      fromLocal(company, Seq(ItemSource.baidu), method, duration, forcedLatest).flatMap(list =>
                        Marshal(list.flatMap(_.news)).to[HttpResponse]
                      )
                  }

                case _ =>
                  fromLocal(company, Seq(ItemSource.baidu), method, duration, forcedLatest).flatMap(list =>
                    Marshal(list).to[HttpResponse]
                  )
              }
            complete(future)
          }
        }
      }
    }

  private def fromLocal(company: String, sources: Traversable[ItemSource.Value], method: String, duration: Int, forcedLatest: String) = {
    val mtd = Try(SearchMethod.withName(method)).getOrElse(SearchMethod.F)
    newsService.
      fetchNews(company, sources, mtd, Duration(duration, TimeUnit.SECONDS), forcedLatest == "y")
  }

  private def fromCrawlerApi(company: String) =
    SystemUtils.httpClient.get(config.getString("crawler.api-uri") + "/api/news")
      .queryParam("companyName" -> company)
      .execute()
      .map { resp =>
        if (resp.getStatusCode != 200)
          throw new RuntimeException(s"crawler-api not found company: $company, return: ${resp.getStatusCode}")

        HttpResponse(
          StatusCodes.OK,
          entity = HttpEntity(ContentType(MediaTypes.`application/json`), resp.getResponseBody(Utils.CHARSET.name()))
        )
      }

}
