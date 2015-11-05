package crawler.news.crawlers

import java.net.URLEncoder
import java.time.LocalDateTime

import akka.util.Timeout
import crawler.news.{SearchMethod, NewsSource}
import crawler.news.model.{NewsPageItem, NewsItem, NewsResult}
import crawler.util.http.HttpClient
import crawler.util.news.contextextractor.ContentExtractor
import crawler.util.time.TimeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext}

/**
 * 百度新闻爬虫
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
class BaiduCrawler(val httpClient: HttpClient)(implicit ec: ExecutionContext) extends NewsCrawler(NewsSource.BAIDU) {

  import crawler.util.JsoupImplicits._

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
      BaiduCrawler.dealTime(source.lastOption.getOrElse("")),
      summary.text().replace(authorText, "").replace(footer, ""),
      "")
  }

  override def fetchNewsList(key: String): Future[NewsResult] =
    fetchPage(BaiduCrawler.BAIDU_NEWS_BASE_URL.format(URLEncoder.encode(key, "UTF-8"))).map { resp =>
      val doc = Jsoup.parse(resp.getResponseBodyAsStream, "UTF-8", "http://news.baidu.com")

      if (doc.getElementById("noresult") ne null) {
        NewsResult(newsSource, key, 0, Nil)
      } else {
        val text = doc
          .getElementById("header_top_bar")
          .getElementsByAttributeValue("class", "nums")
          .first()
          .text()
        val count = """\d+""".r.findAllMatchIn(text).map(_.matched).mkString.toInt

        val newsDiv = doc.getElementById("content_left") // check null

        NewsResult(
          newsSource,
          key,
          count,
          newsDiv.findByClass("result").asScala.map(parseNewsItem).toList)
      }
    }

  override def fetchNewsItem(url: String): Future[NewsPageItem] =
    fetchPage(url).map { resp =>
      val src = resp.getResponseBody("UTF-8")
      val news = ContentExtractor.getNewsByHtml(src)
      NewsPageItem(url, src, news.getTitle, news.getTime, news.getContent)
    }

}

object BaiduCrawler {
  val BAIDU_NEWS_BASE_URL = "http://news.baidu.com/ns?word=%s&tn=news&from=news&cl=2&rn=20&ct=1"
  val TIME_PATTERN = """\d{4}年\d{2}月\d{2}日 \d{2}:\d{2}""".r
  val FEW_HOURS_PATTERN = """(\d+)小时前""".r

  private def dealFewHours(timeStr: String): String = {
    val matcher = FEW_HOURS_PATTERN.pattern.matcher(timeStr)
    if (matcher.matches()) matcher.group(1) else ""
  }

  def dealTime(timeStr: String): String = {
    if (timeStr.length < 2) {
      LocalDateTime.now().format(TimeUtils.formatterDateTime)
    } else if (TIME_PATTERN.pattern.matcher(timeStr).matches()) {
      timeStr.replaceAll( """年|月""", "-").replace("日", "") + ".00"
    } else if (FEW_HOURS_PATTERN.pattern.matcher(timeStr).matches()) {
      val now = LocalDateTime.now()
      val hour = dealFewHours(timeStr).toLong
      now.minusHours(hour).format(TimeUtils.formatterDateTime)
    } else {
      LocalDateTime.now().format(TimeUtils.formatterDateTime)
    }
  }

  def main(args: Array[String]): Unit = {
    import crawler.SystemUtils._
    implicit val timeout = Timeout(10.hours)
    import system.dispatcher

    val httpClient = HttpClient()
    val baidu = new BaiduCrawler(httpClient)
    val f = baidu.run("杭州今元标矩科技有限公司", SearchMethod.F)
    val result = Await.result(f, timeout.duration)
    result.news.foreach(news => println(news.content + "\n\n"))
    println(result.count)

    system.shutdown()
    httpClient.close()
    system.awaitTermination()
    //    System.exit(0)
  }
}
