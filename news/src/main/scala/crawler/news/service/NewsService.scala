package crawler.news.service

import akka.pattern.ask
import crawler.news.commands.{RequestSearchNews, SearchNews}
import crawler.news.crawlers.{BaiduCrawler, NewsCrawler}
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.model.NewsResult
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
  val dbRepo = new NewsDBRepo

  def fetchNews(key: String,
                sources: Seq[NewsSource.Value],
                method: SearchMethod.Value,
                duration: FiniteDuration): Future[Seq[NewsResult]] = {
    val results = dbRepo.findNews(key, sources, method)
    if (results.isEmpty) {
      val msg = RequestSearchNews(sources, SearchNews(key, method, duration))

      // TODO 加上1秒以保存actor内可有充足时间来处理 duration
      newsSupervisor.ask(msg)(duration + 1.seconds).mapTo[Seq[NewsResult]]
    } else {
      Future.successful(results)
    }
  }
}


