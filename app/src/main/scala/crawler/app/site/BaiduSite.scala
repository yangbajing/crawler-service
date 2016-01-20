package crawler.app.site

import java.net.URLEncoder
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import crawler.app.news.crawlers.BaiduNews
import crawler.enums.ItemSource
import crawler.model.{NewsItem, SearchResult}
import crawler.util.Crawler
import crawler.util.http.HttpClient
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
class BaiduSite(val httpClient: HttpClient,
                followUrl: Boolean) extends Crawler with LazyLogging {

  override protected val defaultHeaders: Array[Seq[(String, String)]] =
    super.defaultHeaders.map(headers => headers :+ ("User-Agent" -> "Baiduspider"))

  /**
    * 抓取搜索页
    *
    * @param key 搜索关键词
    * @return
    */
  def fetchItemList(key: String)(implicit ec: ExecutionContext): Future[SearchResult] = {
    val promise = Promise[Seq[NewsItem]]()

    val url = BaiduSite.BAIDU_SITE_BASE_URL.format(URLEncoder.encode(key, "UTF-8"))
    logger.debug("url: " + url)

    val newsResultsFuture = fetchPage(url).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduSite.BAIDU_SITE_HOST).getElementById("wrapper_wrapper")

      logger.debug(doc.toString + "\n\n\n")

      val now = TimeUtils.now()
      val contentNone = doc.select(".content_none")

      logger.debug("contentNone: " + contentNone)

      if (!contentNone.isEmpty) {
        promise.success(Nil)
        SearchResult(ItemSource.baiduSite, key, now, 0, Nil)
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

        SearchResult(
          ItemSource.baiduSite,
          key,
          now,
          count,
          itemResults.map(parseNewsItem).toList)
      }
    }

    for {
      newsResult <- newsResultsFuture
      newsItems <- promise.future
    } yield {
      newsResult.copy(news = newsResult.news ++ newsItems)
    }
  }

  def fetchPageLinks(url: String)(implicit ec: ExecutionContext): Future[Seq[NewsItem]] = {
    fetchPage(url).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduSite.BAIDU_SITE_HOST)
      if (doc.getElementById("content_none") != null) {
        Nil
      } else {
        val itemDiv = doc.getElementById("content_left")
        val itemResults = itemDiv.select(".result.c-container").asScala
        itemResults.map(parseNewsItem).toList
      }
    }
  }

  def parseNewsItem(elem: Element): NewsItem = {
    implicit val ec = ExecutionContext.Implicits.global

    val link = elem.select(".t").select("a").first()
    val href = link.attr("href")
    val url = if (followUrl) {
      Option(Await.result(HttpClient.find302Location(httpClient, href, requestHeaders()), 1.second)).getOrElse(href)
    } else {
      href
    }

    val title = link.text()
    val summary = elem.select(".c-abstract").text()
    val time = BaiduNews.dealTime(elem.select(".newTimeFactor_before_abs").text())

    NewsItem(title, url, ItemSource.baiduSite.toString, time, summary)
  }

}

object BaiduSite {
  val PAGE_LIMIT = 5
  val BAIDU_SITE_BASE_URL = "https://www.baidu.com/s?wd=%s&rsv_spt=1&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_n=2&rsv_sug3=1"
  val BAIDU_SITE_HOST = "https://www.baidu.com"
}
