package crawler.news.service.actors

import akka.actor.Props
import crawler.news.commands.{ItemPageResult, StartFetchItemPage}
import crawler.news.crawlers.NewsCrawler
import crawler.news.enums.NewsSource
import crawler.news.model.{NewsItem, NewsPageItem}
import crawler.news.service.NewsMaster
import crawler.util.actors.MetricActor

import scala.util.{Failure, Success}

/**
 * 详情页面
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class ItemPageWorker(source: NewsSource.Value, newsItem: NewsItem) extends MetricActor {

  import context.dispatcher

  override val metricReceive: Receive = {
    case StartFetchItemPage =>
      val doSender = sender()

      NewsCrawler.getCrawler(source) match {
        case Some(crawler) =>
          crawler.fetchNewsItem(newsItem.url).onComplete {
            case Success(pageItem) =>
              doSender ! ItemPageResult(Right(pageItem))

            case Failure(e) =>
              doSender ! ItemPageResult(Left(e.getLocalizedMessage))
          }

        case None =>
          doSender ! ItemPageResult(Left(s"Crawler $source not exists, ${newsItem.url} needed."))
      }
  }

}

object ItemPageWorker {
  def props(source: NewsSource.Value, item: NewsItem) = Props(new ItemPageWorker(source, item))
}
