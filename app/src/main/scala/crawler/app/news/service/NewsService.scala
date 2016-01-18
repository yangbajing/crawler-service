package crawler.app.news.service

import akka.pattern.ask
import crawler.commands.{RequestSearchNews, SearchNews}
import crawler.enums.{ItemSource, SearchMethod}
import crawler.model.{NewsItem, SearchResult}
import crawler.util.time.TimeUtils

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

  def fetchNewsApi(_key: String,
                   sources: Traversable[ItemSource.Value],
                   method: SearchMethod.Value,
                   duration: FiniteDuration,
                   forcedLatest: Boolean): Future[Seq[NewsItem]] = {
    fetchNews(_key, sources, method, duration, forcedLatest).
      map(_.flatMap(_.news))
  }

  def fetchNews(_key: String,
                sources: Traversable[ItemSource.Value],
                method: SearchMethod.Value,
                duration: FiniteDuration,
                forcedLatest: Boolean): Future[Seq[SearchResult]] = {
    val key = _key.trim
    val future = dbRepo.findNews(key, sources, method, if (forcedLatest) Some(TimeUtils.nowBegin()) else None)

    future.flatMap(results =>
      if (results.isEmpty) {
        val msg = RequestSearchNews(sources.toSeq, SearchNews(key, method, duration))
        // TODO 最长5分钟
        newsMaster.ask(msg)(5.minutes).mapTo[Seq[SearchResult]]
      } else {
        Future.successful(results)
      }
    )
  }

}

