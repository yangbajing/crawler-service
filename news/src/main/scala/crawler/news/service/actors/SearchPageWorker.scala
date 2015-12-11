package crawler.news.service.actors

import akka.actor.Props
import crawler.news.commands.{SearchPageFailure, SearchPageResult, StartFetchSearchPage}
import crawler.news.crawlers.NewsCrawler
import crawler.news.enums.NewsSource
import crawler.util.actors.MetricActor

import scala.util.{Failure, Success}

/**
 * 搜索页面
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class SearchPageWorker(source: NewsSource.Value, key: String) extends MetricActor {

  import context.dispatcher

  override val metricReceive: Receive = {
    case StartFetchSearchPage =>
      val doSender = sender()

      NewsCrawler.getCrawler(source) match {
        case Some(crawler) =>
          crawler.fetchNewsList(key).onComplete {
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
  def props(source: NewsSource.Value, name: String) = Props(new SearchPageWorker(source, name))
}