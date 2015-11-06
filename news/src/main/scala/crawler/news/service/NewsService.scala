package crawler.news.service

import akka.pattern.ask
import crawler.news.commands.{RequestSearchNews, SearchNews}
import crawler.news.crawlers.{BaiduCrawler, NewsCrawler}
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, SearchMethod}
import crawler.util.http.HttpClient

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * 新闻服务
 * Created by yangjing on 15-11-3.
 */
class NewsService(httpClient: HttpClient) {

  import crawler.SystemUtils._
  import system.dispatcher

  NewsCrawler.registerCrawler(NewsSource.BAIDU, new BaiduCrawler(httpClient))
  val newsSupervisor = system.actorOf(NewsMaster.props, NewsMaster.actorName)

  def fetchNews(company: String,
                sources: Seq[NewsSource.Value],
                method: SearchMethod.Value,
                duration: FiniteDuration): Future[Seq[NewsResult]] = {
    val msg = RequestSearchNews(sources, SearchNews(company, method, duration))

    // TODO 加上5秒以保存actor内可有充足时间来处理 duration
    newsSupervisor.ask(msg)(duration + 5.seconds).mapTo[Seq[NewsResult]]
  }
}
