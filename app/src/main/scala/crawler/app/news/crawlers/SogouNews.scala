package crawler.app.news.crawlers

import java.net.URLEncoder

import akka.util.Timeout
import crawler.SystemUtils
import crawler.enums.{ItemSource, SearchMethod}
import crawler.app.news.model.{NewsItem, SearchResult}
import crawler.util.http.HttpClient
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
 * 搜狗新闻搜索
  *
  * @param httpClient
 */
class SogouNews(val httpClient: HttpClient) extends NewsCrawler(ItemSource.sogou) {

  private def parseItem(elem: Element) = {
    val header = elem.select("h3.pt")
    val title = header.select("a.pp")
    val source = header.select("cite") match {
      case s if s.isEmpty => Array("", "")
      case s => s.text().split(SogouNews.CITE_SPLIT_CHAR)
    }
    val summary = elem.select("div.ft").text().replace( """>>\d+?条相同新闻""", "")

    NewsItem(
      title.text(),
      title.attr("href"),
      source(0),
      Option(TimeUtils.toLocalDateTime(source.tail.mkString(" "))),
      summary)
  }

  /**
   * 抓取搜索页
    *
    * @param key 搜索关键词
   * @return
   */
  override def fetchItemList(key: String)(implicit ec: ExecutionContext): Future[SearchResult] = {
    //   val doc =  fetchDocument(SogouCrawler.searchUrl(URLEncoder.encode(key, "UTF-8")))
    fetchPage(SogouNews.searchUrl(URLEncoder.encode(key, "UTF-8"))).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBody, "http://news.sogou.com")
      val now = TimeUtils.now()
      //      println(doc)
      val results = doc.select("div.results")
      if (results.isEmpty) {
        SearchResult(newsSource, key, now, 0, Nil)
      } else {
        val newsList = results.select("div.rb").asScala.map(parseItem)
        var count = Try( """\d+""".r.findAllMatchIn(doc.select("#pagebar_container").select("div.num").text()).mkString.toInt).getOrElse(0)
        if (count < 1) {
          logger.warn("count < 1")
          count = newsList.size
        }
        SearchResult(newsSource, key, now, count, newsList)
      }
    }
  }
}

object SogouNews {
  val REGEX = """\d+?条相同新闻""".r
  val CITE_SPLIT_CHAR = 160.toChar

  def searchUrl(key: String) = s"http://news.sogou.com/news?query=%22$key%22"

  ////////////////////////////////////////////////////////////////////////////
  // 以下为测试用例
  ////////////////////////////////////////////////////////////////////////////

  def run(newsCrawler: NewsCrawler,
          key: String,
          method: SearchMethod.Value)(implicit ec: ExecutionContext): Future[SearchResult] = {
    val newsResult = newsCrawler.fetchItemList(key)
    if (SearchMethod.A == method) {
      newsResult
    } else {
      newsResult.flatMap { result =>
        val seqs = result.news.map { news =>
          //          newsCrawler.fetchPage(news.url).map { resp =>
          //            (news.url, ContentExtractor.getNewsByHtml(resp.getResponseBody("UTF-8")).getContent)
          //          }
          newsCrawler.fetchNewsItem(news.url)
        }
        val f = Future.sequence(seqs)
        f.map { pageItems =>
          val news = result.news.map { news =>
            pageItems.find(_.url == news.url) match {
              case Some(pageItem) =>
                news.copy(content = Option(pageItem.content))
              case None =>
                news
            }
          }
          result.copy(news = news)
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    import SystemUtils._
    import system.dispatcher

    import scala.concurrent.duration._

    implicit val timeout = Timeout(10.hours)

    val httpClient = HttpClient()
    val sogou = new SogouNews(httpClient)
    val f = run(sogou, "杭州今元标矩科技有限公司", SearchMethod.F)
    val result = Await.result(f, timeout.duration)
        result.news.foreach(println)


    //    val doc = sogou.fetchDocument(searchUrl(URLEncoder.encode("杭州今元标矩科技有限公司", "UTF-8")))
    //    println(doc)

    system.shutdown()
    httpClient.close()
    system.awaitTermination()
  }
}
