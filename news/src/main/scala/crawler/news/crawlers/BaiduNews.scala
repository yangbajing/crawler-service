package crawler.news.crawlers

import java.net.URLEncoder
import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import crawler.SystemUtils
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.model.{NewsItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Promise, Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * 百度新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
class BaiduNews(val httpClient: HttpClient) extends NewsCrawler(NewsSource.baidu) {

  import crawler.util.JsoupImplicits._

  override protected val defaultHeaders: Array[Seq[(String, String)]] =
    super.defaultHeaders.map(headers => headers :+ ("User-Agent" -> "Baiduspider"))

  private def parseNewsItem(news: Element): NewsItem = {
    val a = news.findByClass("c-title").first().getElementsByTag("a").first()
    val summary = news.findByClass("c-summary")
    val authorText = news.findByClass("c-author").text()
    val source = authorText.split("  ")
    val footer = summary.findByClass("c-info").first().text()
    NewsItem(
      a.text(),
      a.attr("href"),
      source.headOption.getOrElse(""),
      BaiduNews.dealTime(source.lastOption.getOrElse("")),
      summary.text().replace(authorText, "").replace(footer, ""))
  }

  override def fetchNewsList(key: String)(implicit ec: ExecutionContext): Future[NewsResult] = {
    val promise = Promise[Seq[NewsItem]]()

    val newsResultsFuture = fetchPage(BaiduNews.BAIDU_NEWS_BASE_URL.format(URLEncoder.encode('"' + key + '"', "UTF-8"))).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduNews.BAIDU_NEWS_HOST)
      //        logger.debug(doc.body().toString + "\n\n\n")
      val now = TimeUtils.now()
      if (doc.getElementById("noresult") != null) {
        NewsResult(newsSource, key, now, 0, Nil)
      } else {
        val countText = doc
          .getElementById("header_top_bar")
          .getElementsByAttributeValue("class", "nums")
          .first()
          .text()
        val count =
          """\d+""".r.findAllMatchIn(countText).map(_.matched).mkString.toInt

        val newsDiv = doc.getElementById("content_left")
        val pages = doc.select("#page a").asScala
        val newsItemFutures = pages.take(BaiduNews.PAGE_LIMIT - 1).map { page =>
          TimeUnit.MILLISECONDS.sleep(500)
          fetchPageLinks(BaiduNews.BAIDU_NEWS_HOST + page.attr("href"))
        }
        Future.sequence(newsItemFutures).map(_.flatten).onComplete {
          case Success(list) =>
            promise.success(list)
          case Failure(e) =>
            e.printStackTrace()
            promise.success(Nil)
        }

        NewsResult(
          newsSource,
          key,
          now,
          count,
          newsDiv.findByClass("result").asScala.map(parseNewsItem).toList)
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
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", BaiduNews.BAIDU_NEWS_HOST)
      if (doc.getElementById("noresult") != null) {
        Nil
      } else {
        val newsDiv = doc.getElementById("content_left")
        newsDiv.findByClass("result").asScala.map(parseNewsItem).toList
      }
    }
  }
}

object BaiduNews {
  val PAGE_LIMIT = 5
  val BAIDU_NEWS_HOST = "http://news.baidu.com"
  val BAIDU_NEWS_BASE_URL = "http://news.baidu.com/ns?word=%s&tn=news&from=news&cl=2&rn=20&ct=1"
  val TIME_PATTERN = """\d{4}年\d{2}月\d{2}日 \d{2}:\d{2}""".r
  val FEW_HOURS_PATTERN = """(\d+)小时前""".r

  private def dealFewHours(timeStr: String): String = {
    val matcher = FEW_HOURS_PATTERN.pattern.matcher(timeStr)
    if (matcher.matches()) matcher.group(1) else ""
  }

  def dealTime(timeStr: String): LocalDateTime = {
    if (timeStr.length < 2) {
      LocalDateTime.now()
    } else if (TIME_PATTERN.pattern.matcher(timeStr).matches()) {
      val s = timeStr.replaceAll( """年|月""", "-").replace("日", "") + ":00"
      LocalDateTime.parse(s, TimeUtils.formatterDateTime)
    } else if (FEW_HOURS_PATTERN.pattern.matcher(timeStr).matches()) {
      val now = LocalDateTime.now()
      val hour = dealFewHours(timeStr).toLong
      now.minusHours(hour)
    } else {
      LocalDateTime.now()
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // 以下为测试用例
  ////////////////////////////////////////////////////////////////////////////

  def run(newsCrawler: NewsCrawler,
          name: String,
          method: SearchMethod.Value)(implicit ec: ExecutionContext): Future[NewsResult] = {
    val newsResult = newsCrawler.fetchNewsList(name)
    if (SearchMethod.A == method) {
      newsResult
    } else {
      newsResult.flatMap { result =>
        val seqs = result.news.map { news =>
          newsCrawler.fetchPage(news.url).map { resp =>
            (news.url, ContentExtractor.getNewsByHtml(resp.getResponseBody("UTF-8")).getContent)
          }
        }
        val f = Future.sequence(seqs)
        f.map { urlContents =>
          val news = result.news.map { news =>
            urlContents.find(_._1 == news.url) match {
              case Some((_, content)) =>
                news.copy(content = Option(content))
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
    implicit val timeout = Timeout(10.hours)
    import system.dispatcher

    val httpClient = HttpClient()
    val baidu = new BaiduNews(httpClient)
    val f = run(baidu, "杭州今元标矩科技有限公司", SearchMethod.F)
    val result = Await.result(f, timeout.duration)
    result.news.foreach(news => println(news.content + "\n\n"))
    println(result.count)

    system.shutdown()
    httpClient.close()
    system.awaitTermination()
    //    System.exit(0)
  }
}
