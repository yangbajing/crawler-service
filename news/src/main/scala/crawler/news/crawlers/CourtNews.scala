package crawler.news.crawlers

import java.time.LocalDate

import crawler.SystemUtils
import crawler.news.enums.NewsSource
import crawler.news.model.{NewsItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.time.DateTimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 中国法院网新闻搜索
 * Created by yangjing on 15-11-9.
 */
class CourtNews(val httpClient: HttpClient) extends NewsCrawler(NewsSource.COURT) {
  private def fetchPagePost(url: String, data: Seq[(String, String)]) = {
    httpClient.post(url).header(defaultHeaders: _*).addFormParam(data: _*).execute()
  }

  private def parseNewsItem(elem: Element) = {
    println(elem)
    val a = elem.select("dt").select("a").first()
    val dds = elem.select("dd")
    val item = NewsItem(
      a.text(),
      CourtNews.SITE_URL + a.attr("href"),
      "中国法院网",
      DateTimeUtils.toLocalDateTime(dds.last().text().split("    ").last),
      dds.first().text())
    println(item)
    item
  }

  /**
   * 抓取搜索页
   * @param key 搜索关键词
   * @return
   */
  override def fetchNewsList(key: String)(implicit ec: ExecutionContext): Future[NewsResult] = {
    fetchPagePost(CourtNews.SEARCH_URL, Seq(
      "keyword" -> key,
      "button" -> "提交",
      "content_time_publish_begin" -> "2002-01-01",
      "content_time_publish_end" -> LocalDate.now().toString,
      "article_category_id" -> "",
      "content_author" -> ""
    )).map { resp =>
      val now = DateTimeUtils.now()
      val doc = Jsoup.parse(resp.getResponseBody(SystemUtils.DEFAULT_CHARSET.name), CourtNews.SITE_URL)
      val newsDl = doc.select("div.search_content").select("dl")
      if (newsDl.isEmpty) {
        NewsResult(newsSource, key, now, 0, Nil)
      } else {
        val newsItems = newsDl.asScala.map(parseNewsItem)
        val countText = doc.select("div.search_br").select("span").first().text
        val count =
          try {
            countText.toInt
          } catch {
            case e: Exception =>
              logger.warn("count < 1: " + countText)
              0
          }

        NewsResult(newsSource, key, now, count, newsItems)
      }
    }
  }
}

object CourtNews {
  val SITE_URL = "http://www.chinacourt.org"
  val SEARCH_URL = "http://www.chinacourt.org/article/search.shtml"
}
