package crawler.app.site

import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import crawler.app.news.model.SearchRequest
import crawler.app.site.model.{SiteItem, SiteResult}
import crawler.enums.ItemSource
import crawler.util.Crawler
import crawler.util.http.HttpClient
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
class BaiduSite(val httpClient: HttpClient,
                searchRequest: SearchRequest) extends Crawler with LazyLogging {

  override protected val defaultHeaders: Array[Seq[(String, String)]] =
    super.defaultHeaders.map(headers => headers :+ ("User-Agent" -> "Baiduspider"))

  val values = searchRequest.params.map(_.value)

  /**
    * 抓取搜索页
    *
    * @return
    */
  def fetchItemList()(implicit ec: ExecutionContext): Future[SiteResult] = {
    val promise = Promise[Seq[SiteItem]]()
    val key = searchRequest.toParam

    val url = BaiduSite.BAIDU_SITE_BASE_URL.format(URLEncoder.encode(key, "UTF-8"))
    logger.info(s"key: $key, url: $url")

    val newsResultsFuture = fetchPage(url).flatMap { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduSite.BAIDU_SITE_HOST).getElementById("wrapper_wrapper")
      val now = TimeUtils.now()
      val contentNone = doc.select(".content_none")

      if (!contentNone.isEmpty) {
        promise.success(Nil)
        Future.successful(SiteResult(ItemSource.baiduSite, key, now, 0, Nil))
      } else {
        val wrapper = doc
        val countText = wrapper
          .select(".head_nums_cont_outer.OP_LOG")
          .select(".nums")
          .text()
        val count =
          """\d+""".r.findAllMatchIn(countText).map(_.matched).mkString.toInt

        val itemDiv = doc.getElementById("content_left")
        val itemResults = itemDiv.select(".result.c-container").asScala

        val pages = doc.select("#page a").asScala
        val newsItemFutures = pages.take(BaiduSite.PAGE_LIMIT - 1).map { page =>
          TimeUnit.MILLISECONDS.sleep(100)
          fetchPageLinks(BaiduSite.BAIDU_SITE_HOST + page.attr("href"))
        }

        Future.sequence(newsItemFutures).map(_.flatten).onComplete {
          case Success(list) =>
            promise.success(list)
          case Failure(e) =>
            e.printStackTrace()
            promise.success(Nil)
        }

        Future.sequence(itemResults.map(parseSiteItem))
          .map(items => SiteResult(ItemSource.baiduSite, key, now, count, items))
      }
    }

    for {
      newsResult <- newsResultsFuture
      newsItems <- promise.future
    } yield {
      newsResult.copy(items = newsResult.items ++ newsItems)
    }
  }

  def fetchPageLinks(url: String)(implicit ec: ExecutionContext): Future[Seq[SiteItem]] = {
    fetchPage(url).flatMap { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduSite.BAIDU_SITE_HOST)
      if (doc.getElementById("content_none") != null) {
        Future.successful(Nil)
      } else {
        val itemDiv = doc.getElementById("content_left")
        val itemResults = itemDiv.select(".result.c-container").asScala
        val futures = itemResults.map(parseSiteItem)
        Future.sequence(futures)
      }
    }
  }

  def parseSiteItem(elem: Element)(implicit ec: ExecutionContext): Future[SiteItem] = {
    val link = elem.select(".t").select("a").first()
    val href = link.attr("href")

    extractPageUrl(href).map { url =>
      val title = link.text()

      val sourceHostDesc = elem.select(".f13 a").first().text()
      val source = sourceHostDesc.take(sourceHostDesc.indexOf('/'))

      val abstractElem = elem.select(".c-abstract")
      val summary = abstractElem.asScala.filterNot(e => e.attr("class").contains("newTimeFactor_before_abs")).map(_.text()).mkString

      //    val summary = elem.select(".c-abstract").text()
      val time = BaiduSite.dealTime(abstractElem.select(".newTimeFactor_before_abs").text())

      SiteItem(title, url, source, time, summary, values)
    }
  }

  def extractPageUrl(href: String): Future[String] = {
    implicit val ec = ExecutionContext.Implicits.global

    if (searchRequest.followUrl) {
      HttpClient.find302Location(httpClient, href, requestHeaders()).map(v => if (v == null) href else v)
    } else {
      Future.successful(href)
    }
  }

}

object BaiduSite {
  // 抓取前５页
  val PAGE_LIMIT = 5

  val BAIDU_SITE_BASE_URL = "https://www.baidu.com/s?wd=%s&rsv_spt=1&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_n=2&rsv_sug3=1"

  val BAIDU_SITE_HOST = "https://www.baidu.com"

  val TIME_PATTERN = """(\d{4})年(\d{1,2})月(\d{1,2})日""".r

  def dealTime(timeStr: String): Option[LocalDateTime] = timeStr.substring(0, timeStr.indexOf('日') + 1) match {
    case TIME_PATTERN(year, month, day) => Some(LocalDateTime.of(year.toInt, month.toInt, day.toInt, 0, 0))
    case _ => None
  }

}
