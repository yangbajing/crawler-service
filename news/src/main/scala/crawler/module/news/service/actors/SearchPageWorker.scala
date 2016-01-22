package crawler.module.news.service.actors

import akka.actor.Props
import crawler.module.news.commands.{SearchPageFailure, SearchPageResult, StartFetchSearchPage}
import crawler.module.news.crawlers.NewsCrawler
import crawler.module.news.enums.ItemSource
import crawler.util.actors.MetricActor

import scala.util.{Failure, Success}

/**
 * 搜索页面
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class SearchPageWorker(source: ItemSource.Value, key: String) extends MetricActor {

  import context.dispatcher

  override val metricReceive: Receive = {
    case StartFetchSearchPage =>
      val doSender = sender()

      NewsCrawler.getCrawler(source) match {
        case Some(crawler) =>
          crawler.fetchItemList(key).onComplete {
            case Success(result) =>
              doSender ! SearchPageResult(result)
              stop()

            case Failure(e) =>
              doSender ! SearchPageFailure(e)
              stop()
          }

        case None =>
          doSender ! SearchPageFailure(new RuntimeException(s"Crawler $source not exists"))
          stop()
      }
  }

  private def stop(): Unit = context.stop(self)
}

object SearchPageWorker {

  def props(source: ItemSource.Value, name: String) = Props(new SearchPageWorker(source, name))

}