package crawler.news.crawlers

import crawler.news.enums.{SearchMethod, NewsSource}
import crawler.news.model.{NewsPageItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}

/**
 * 新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
abstract class NewsCrawler(val newsSource: NewsSource.Value) {
  //  val name: String
  val httpClient: HttpClient

  protected def defaultHeaders = Seq(
    "User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")

  def fetchPage(url: String) = {
    httpClient.get(url).header(defaultHeaders: _*).execute()
  }

  def fetchDocument(url: String) = {
    val conn = Jsoup.connect(url)
    defaultHeaders.foreach { case (name, value) => conn.header(name, value) }
    conn.execute().parse()
  }

  /**
   * 抓取搜索页
   * @param key 搜索关键词
   * @return
   */
  def fetchNewsList(key: String): Future[NewsResult]

  /**
   * 抓取新闻详情页
   * @param url 网页链接
   * @return
   */
  def fetchNewsItem(url: String)(implicit ec: ExecutionContext): Future[NewsPageItem] = {
    //    fetchPage(url).map { resp =>
    //      val src = resp.getResponseBody("UTF-8")
    //      try {
    //        val news = ContentExtractor.getNewsByHtml(src)
    //        NewsPageItem(url, src, news.getTitle, news.getTime, news.getContent)
    //      } catch {
    //        case e: Exception =>
    //          println(src)
    //          NewsPageItem(url, src, "", "", "")
    //      }
    //    }
    Future {
      val document = fetchDocument(url)
      val news = ContentExtractor.getNewsByDoc(document)
      NewsPageItem(url, document.toString, news.getTitle, news.getTime, news.getContent)
    }.recover {
      case e: Exception =>
        NewsPageItem(url, "", "", "", "")
    }
  }

}

object NewsCrawler {
  private var _newsCrawler = Map.empty[NewsSource.Value, NewsCrawler]

  def registerCrawler(source: NewsSource.Value, newsCrawler: NewsCrawler): Unit = {
    _newsCrawler = _newsCrawler + (source -> newsCrawler)
  }

  def getCrawler(source: NewsSource.Value): Option[NewsCrawler] = _newsCrawler.get(source)

}
