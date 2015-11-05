package crawler.news.crawlers

import crawler.news.{SearchMethod, NewsSource}
import crawler.news.model.{NewsPageItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor

import scala.concurrent.{ExecutionContext, Future}

/**
 * 新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
abstract class NewsCrawler(val newsSource: NewsSource.Value) {
  //  val name: String
  val httpClient: HttpClient

  protected val defaultHeaders = Seq(
    "User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")

  protected def fetchPage(url: String) = {
    httpClient.get(url).header(defaultHeaders: _*).execute()
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
  def fetchNewsItem(url: String): Future[NewsPageItem]

  def run(name: String, method: SearchMethod.Value)(implicit ec: ExecutionContext): Future[NewsResult] = {
    val newsResult = fetchNewsList(name)
    if (SearchMethod.S == method) {
      newsResult
    } else {
      newsResult.flatMap { result =>
        val seqs = result.news.map { news =>
          fetchPage(news.url).map { resp =>
            (news.url, ContentExtractor.getNewsByHtml(resp.getResponseBody("UTF-8")).getContent)
          }
        }
        val f = Future.sequence(seqs)
        f.map { urlContents =>
          val news = result.news.map { news =>
            urlContents.find(_._1 == news.url) match {
              case Some((_, content)) =>
                news.copy(content = content)
              case None =>
                news
            }
          }
          result.copy(news = news)
        }
      }
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
