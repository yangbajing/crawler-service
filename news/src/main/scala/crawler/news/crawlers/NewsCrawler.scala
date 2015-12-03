package crawler.news.crawlers

import com.typesafe.scalalogging.LazyLogging
import crawler.news.NewsUtils
import crawler.news.enums.NewsSource
import crawler.news.model.{NewsPageItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor
import org.jsoup.helper.DataUtil

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
abstract class NewsCrawler(val newsSource: NewsSource.Value) extends LazyLogging {
  //  val name: String
  val httpClient: HttpClient

  protected def defaultHeaders = Array(
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
      "Accept-Encoding" -> "gzip, deflate, sdch",
      "Accept-Language" -> "zh-CN,zh;q=0.8,en;q=0.6",
      "Connection" -> "keep-alive"
    ),
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/601.2.7 (KHTML, like Gecko) Version/9.0.1 Safari/601.2.7",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    ),
    Seq(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:39.0) Gecko/20100101 Firefox/39.0",
      "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
      "Accept-Encoding" -> "gzip, deflate",
      "Accept-Language" -> "en-US,en;q=0.5",
      "Connection" -> "keep-alive"
    )
  )

  def requestHeaders = defaultHeaders(Random.nextInt(defaultHeaders.length))

  def fetchPage(url: String) = {
    val headers = defaultHeaders(Random.nextInt(defaultHeaders.length))
    //    println("url: " + url)
    //    headers.foreach(println)

    httpClient.get(url).setFollowRedirects(true).header(headers: _*).execute()
  }

  /**
   * 抓取搜索页
   * @param key 搜索关键词
   * @return
   */
  def fetchNewsList(key: String)(implicit ec: ExecutionContext): Future[NewsResult]

  /**
   * 抓取新闻详情页
   * @param url 网页链接
   * @return
   */
  def fetchNewsItem(url: String)(implicit ec: ExecutionContext): Future[NewsPageItem] = {
    fetchPage(url).map { resp =>
      val in = resp.getResponseBodyAsStream
      val doc = DataUtil.load(in, null, NewsUtils.uriToBaseUri(url))
      val src = doc.toString
//      try {
        val news = ContentExtractor.getNewsByDoc(doc)
        NewsPageItem(url, src, /*news.getTitle, news.getTime,*/ news.getContent)
//      } catch {
//        case e: Exception =>
//          logger.warn(s"$url context extractor", e)
//          NewsPageItem(url, src, /*"", "",*/ "")
//      }
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
