package crawler.news.service.actor

import java.util.concurrent.TimeUnit

import akka.actor.{PoisonPill, ActorRef, Actor, Props}
import crawler.news.commands._
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, SearchMethod}
import crawler.util.actors.MetricActor

/**
 * 新闻job
 * Created by yangjing on 15-11-4.
 */
class NewsJob(method: SearchMethod.Value) extends MetricActor {
  @volatile var _reqSender: ActorRef = null

  override def metricReceive: Receive = {
    case s@SearchNews(name, source) =>
      _reqSender = sender()
      logger.info(s"${self.path} 收到消息：$s")
      //      sender() ! s

      val searchPage = context.actorOf(SearchPageWorker.props(name, source), "search-page")
      searchPage ! StartFetchSearchPage

    case newsResult: NewsResult =>
      logger.info(s"获得新闻结果：${newsResult.count}")
      _reqSender ! newsResult
      //      context.stop(self)
      self ! PoisonPill
  }

  @throws[Exception](classOf[Exception]) override
  def postStop(): Unit = {
    // TODO 数据持久化
    super.postStop()
  }
}

object NewsJob {
  def props(method: SearchMethod.Value) = Props(new NewsJob(method))
}

class SearchPageWorker(name: String,
                       source: NewsSource.Value) extends MetricActor {

  override def metricReceive: Actor.Receive = {
    case StartFetchSearchPage =>
      logger.info(s"${self.path} 收到消息：$StartFetchSearchPage")
      val doSender = sender()

      // sleep 10s 模拟抓取
      TimeUnit.SECONDS.sleep(10)

      doSender ! NewsResult(source.toString, name, 0, Nil)
  }
}

object SearchPageWorker {
  def props(name: String, source: NewsSource.Value) = Props(new SearchPageWorker(name, source))
}

class ItemPageWorker extends MetricActor {
  override def metricReceive: Actor.Receive = {
    case s =>
      logger.info(s"${self.path} 收到消息：$s")
  }
}

object ItemPageWorker {
  def props = Props[ItemPageWorker]
}
