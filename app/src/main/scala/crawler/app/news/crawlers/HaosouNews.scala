package crawler.app.news.crawlers

import java.net.URLEncoder

import crawler.SystemUtils
import crawler.app.news.NewsUtils
import crawler.enums.ItemSource
import crawler.model.{NewsItem, SearchResult}
import crawler.util.http.HttpClient
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
 * 360好搜新闻搜索
 * Created by yangjing on 15-11-9.
 */
class HaosouNews(val httpClient: HttpClient) extends NewsCrawler(ItemSource.haosou) {
  private def parseItem(elem: Element) = {
    val a = elem.select("a")
    val newsInfo = elem.select("p.newsinfo")
    NewsItem(
      a.text(),
      a.attr("href"),
      newsInfo.select("span.sitename").text(),
      TimeUtils.toLocalDateTime(newsInfo.select("span.posttime").attr("title")),
      elem.select("p.content").text())
  }

  /**
   * 抓取搜索页
    *
    * @param key 搜索关键词
   * @return
   */
  override def fetchItemList(key: String)(implicit ec: ExecutionContext): Future[SearchResult] = {
    fetchPage(HaosouNews.searchUrl(key)).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBody(SystemUtils.DEFAULT_CHARSET.name()), NewsUtils.uriToBaseUri(HaosouNews.SEARCH_SITE))
      val now = TimeUtils.now()
      val ul = doc.select("ul#news")
      if (ul.isEmpty) {
        SearchResult(newsSource, key, now, 0, Nil)
      } else {
        val newsItems = ul.select("li.res-list").asScala.map(parseItem)
        val countText = doc.select("div#page").select("span.nums").text()
        val count =
          try {
            """\d+""".r.findAllMatchIn(countText).mkString.toInt
          } catch {
            case e: Exception =>
              logger.warn("count < 1")
              newsItems.size
          }
        SearchResult(newsSource, key, now, count, newsItems)
      }
    }
  }
}

object HaosouNews {
  val SEARCH_SITE = "http://news.haosou.com"

  def searchUrl(key: String) = SEARCH_SITE + "/ns?q=%s".format(URLEncoder.encode(key, SystemUtils.DEFAULT_CHARSET.name()))

}
