package crawler.news.service

import crawler.news.contextextractor.ContentExtractor
import crawler.news.model.NewsResult
import crawler.util.http.HttpClient
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
trait NewsCrawler {
  val name: String
  val key: String
  val httpClient: HttpClient

  protected val defaultHeaders = Seq(
    "User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")

  protected def fetchSearchPage(url: String) = {
    httpClient.get(url).header(defaultHeaders: _*).execute()
  }

  def run(method: String)(implicit ec: ExecutionContext): Future[NewsResult] = {
    val newsResult = fetchNewsList()
    if ("a" == method.toString) {
      newsResult
    } else {
      newsResult.flatMap { result =>
        val seqs = result.news.map { news =>
          fetchSearchPage(news.url).map { resp =>
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

  def fetchNewsList(): Future[NewsResult]
}
