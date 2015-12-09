package crawler.news.service

import akka.pattern.ask
import crawler.news.commands.{RequestSearchNews, SearchNews}
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.model.NewsResult
import crawler.util.time.DateTimeUtils

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * 新闻服务
 * Created by yangjing on 15-11-3.
 */
class NewsService {

  import crawler.SystemUtils._
  import system.dispatcher

  val newsMaster = system.actorOf(NewsMaster.props, NewsMaster.actorName)
  val dbRepo = new NewsDBRepo

  def fetchNews(_key: String,
                sources: Seq[NewsSource.Value],
                method: SearchMethod.Value,
                duration: FiniteDuration,
                forcedLatest: Boolean): Future[Seq[NewsResult]] = {
    val key = _key.trim
    val future = dbRepo.findNews(key, sources, method, if (forcedLatest) Some(DateTimeUtils.nowBegin()) else None)

    future.flatMap {
      case Nil =>
        val msg = RequestSearchNews(sources, SearchNews(key, method, duration))
        newsMaster.ask(msg)(duration - 100.milliseconds).mapTo[Seq[NewsResult]]
      case results =>
        Future.successful(results)
    }
  }

}


