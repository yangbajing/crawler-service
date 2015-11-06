package crawler.news.service.actors

import akka.actor.Props
import crawler.news.commands.{SearchPageFailure, SearchResult, StartFetchSearchPage}
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
              doSender ! SearchResult(result)
            case Failure(e) =>
              doSender ! SearchPageFailure(e)
          }

        case None =>
          doSender ! SearchPageFailure(new RuntimeException(s"Crawler $source not exists"))
      }

    /*
    // sleep 5s 模拟抓取耗时
    TimeUnit.SECONDS.sleep(5)

    val news = NewsResult(source, name, 0,
      Seq(
        NewsItem("测试", "http://hostname.com/news/1", source.toString, "2015-10-23 22:22:22", "这里是内部摘要", ""),
        NewsItem("测试", "http://hostname.com/news/2", source.toString, "2015-10-23 23:22:22", "这里是内部摘要", ""),
        NewsItem("测试", "http://hostname.com/news/3", source.toString, "2015-10-24 00:22:22", "这里是内部摘要", "")
      ))
    doSender ! SearchResult(news)
    */
  }
}

object SearchPageWorker {
  def props(source: NewsSource.Value, name: String) = Props(new SearchPageWorker(source, name))
}