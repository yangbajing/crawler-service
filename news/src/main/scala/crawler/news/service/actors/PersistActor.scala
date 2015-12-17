package crawler.news.service.actors

import akka.actor.Props
import crawler.news.model.{NewsPage, NewsResult}
import crawler.news.service.NewsDBRepo
import crawler.util.actors.MetricActor

/**
  * 持久化
  * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
  */
class PersistActor extends MetricActor {
  val dbRepo = new NewsDBRepo

  override val metricReceive: Receive = {
    case newsResult: NewsResult =>
      dbRepo.saveToSearchPage(newsResult)

      newsResult.news.foreach { item =>
        val page = NewsPage(item.url, item.title, item.source, item.time, item.`abstract`, item.content.getOrElse(""))
        dbRepo.saveToNewsPage(page)
      }
  }

}

object PersistActor {
  val BATCH_SIZE = 20
  val actorName = "persist"

  def props = Props(new PersistActor)
}
