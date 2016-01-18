package crawler.app.news.service.actors

import akka.actor.Props
import crawler.app.news.crawlers.NewsCrawler
import crawler.commands.{ItemPageResult, StartFetchItemPage}
import crawler.enums.ItemSource
import crawler.model.NewsItem
import crawler.util.actors.MetricActor

import scala.util.{Failure, Success}

/**
 * 详情页面
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class ItemPageWorker(source: ItemSource.Value, newsItem: NewsItem) extends MetricActor {

  import context.dispatcher

  override val metricReceive: Receive = {
    case StartFetchItemPage =>
      val doSender = sender()

      NewsCrawler.getCrawler(source) match {
        case Some(crawler) =>
          crawler.fetchNewsItem(newsItem.url).onComplete {
            case Success(pageItem) =>
              logger.debug(s"${newsItem.url} context OK")
              doSender ! ItemPageResult(Right(pageItem))

            case Failure(e) =>
              logger.warn(s"${newsItem.url} context extractor")
              e.printStackTrace()
              doSender ! ItemPageResult(Left(e.getLocalizedMessage))
          }

        case None =>
          doSender ! ItemPageResult(Left(s"Crawler $source not exists, ${newsItem.url} needed."))
      }
  }

}

object ItemPageWorker {
  def props(source: ItemSource.Value, item: NewsItem) = Props(new ItemPageWorker(source, item))
}
