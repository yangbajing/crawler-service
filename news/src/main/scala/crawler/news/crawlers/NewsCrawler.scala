package crawler.news.crawlers

import java.net.URI
import java.nio.charset.Charset

import com.typesafe.scalalogging.LazyLogging
import crawler.SystemUtils
import crawler.news.NewsUtils
import crawler.news.enums.{SearchMethod, NewsSource}
import crawler.news.model.{NewsPageItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor
import org.jsoup.Jsoup
import org.jsoup.helper.DataUtil

import scala.concurrent.{ExecutionContext, Future}

/**
 * 新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
abstract class NewsCrawler(val newsSource: NewsSource.Value) extends LazyLogging {
  //  val name: String
  val httpClient: HttpClient

  protected def defaultHeaders = Seq(
    "User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")

  def fetchPage(url: String) = {
    println("url: " + url)
    httpClient.get(url).header(defaultHeaders: _*).execute()
  }

  private def fetchDocument(url: String) = {
    val conn = Jsoup.connect(url).timeout(10).followRedirects(true)
    defaultHeaders.foreach { case (name, value) => conn.header(name, value) }
    conn.execute().parse()
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
      try {
        val news = ContentExtractor.getNewsByDoc(doc)
        NewsPageItem(url, src, news.getTitle, news.getTime, news.getContent)
      } catch {
        case e: Exception =>
          logger.warn(s"$url context extractor", e)
          NewsPageItem(url, src, "", "", "")
      }
    }
    //    Future {
    //      val document = fetchDocument(url)
    //      val charset = document.charset()
    //      val news = ContentExtractor.getNewsByDoc(document)
    //      val content = news.getContent
    //
    //      if (charset != SystemUtils.DEFAULT_CHARSET) {
    //        println(charset)
    //        println(content)
    //      }
    //
    //      NewsPageItem(url, document.toString, news.getTitle, news.getTime, content)
    //    }.recover {
    //      case e: Exception =>
    //        e.printStackTrace()
    //        NewsPageItem(url, "", "", "", "")
    //    }
  }

}

object NewsCrawler {
  private var _newsCrawler = Map.empty[NewsSource.Value, NewsCrawler]

  def registerCrawler(source: NewsSource.Value, newsCrawler: NewsCrawler): Unit = {
    _newsCrawler = _newsCrawler + (source -> newsCrawler)
  }

  def getCrawler(source: NewsSource.Value): Option[NewsCrawler] = _newsCrawler.get(source)

}
