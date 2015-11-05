package crawler.news.service.actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.AskTimeoutException
import crawler.news.commands._
import crawler.news.model.{NewsItem, NewsResult}
import crawler.news.{NewsSource, NewsUtils, SearchMethod}
import crawler.util.actors.MetricActor

import scala.concurrent.duration.FiniteDuration

/**
 * 新闻job
 * @param method 搜索方式
 * @param duration 持续时间，到期后向客户端返回Timeout。children actor继续业务处理
 */
class NewsJob(method: SearchMethod.Value, duration: FiniteDuration) extends MetricActor {
  @volatile var _reqSender: ActorRef = null
  @volatile var _newsResult: NewsUtils.NewsResultType = null
  @volatile var _isTimeout: Boolean = false
  @volatile var _notCompleteItemPageActorNames = Seq.empty[String]

  import context.dispatcher

  override def preStart(): Unit = {
    super.preStart()
    // XXX 定义超时时间
    context.system.scheduler.scheduleOnce(duration, self, SearchTimeout)
  }

  override def postStop(): Unit = {
    // TODO 数据持久化。可将数据扔给MQ，由挂在MQ上的一个独立进程来执行数据持久化任务。
    _newsResult match {
      case Right(newsResult) =>
        logger.info("已将数据传给MQ: " + newsResult)
      case Left(e) =>
        logger.warn("获取新闻失败: " + e)
    }

    super.postStop()
  }

  override val metricReceive: Receive = {
    case s@SearchNews(name, source, doSender) =>
      _reqSender = doSender
      val searchPage = context.actorOf(SearchPageWorker.props(name, source), "search-page")
      searchPage ! StartFetchSearchPage

    case SearchResult(newsResult) =>
      _newsResult = Right(newsResult)
      method match {
        case SearchMethod.F => // 需要抓取详情内容
          _notCompleteItemPageActorNames = newsResult.news.zipWithIndex.map { case (item, idx) =>
            val childName = "item-" + idx
            val itemPage = context.actorOf(ItemPageWorker.props(item), childName)
            itemPage ! StartFetchItemPage
            childName
          }

        case SearchMethod.S => // 只抓取摘要
          if (!_isTimeout) {
            _reqSender ! newsResult
          }
          self ! PoisonPill // context.stop(self)
      }

    case ItemPageResult(newItem) =>
      val doSender = sender()

      _notCompleteItemPageActorNames = _notCompleteItemPageActorNames.filterNot(_ == doSender.path.name)

      // 更新 result.news
      _newsResult = _newsResult.right.map { result =>
        val news = result.news.map(oldItem => if (oldItem.url == newItem.url) newItem else oldItem)
        result.copy(news = news)
      }

      if (_notCompleteItemPageActorNames.isEmpty) {
        if (!_isTimeout) {
          _reqSender ! _newsResult
        }

        self ! PoisonPill // context.stop(self)
      }

    case SearchTimeout =>
      _isTimeout = true

      // 此时向调用客户端返回超时，但实际的新闻抓取流程仍将继续
      _reqSender ! Left(new AskTimeoutException("搜索超时"))

    case bind@SearchFailure(e) =>
      if (!_isTimeout) {
        _reqSender ! Left(e)
      }
      self ! PoisonPill // context.stop(self)
  }

}

object NewsJob {
  def props(method: SearchMethod.Value, duration: FiniteDuration) =
    Props(new NewsJob(method, duration))
}

class SearchPageWorker(name: String,
                       source: NewsSource.Value) extends MetricActor {

  override val metricReceive: Actor.Receive = {
    case StartFetchSearchPage =>
      val doSender = sender()

      // sleep 5s 模拟抓取耗时
      TimeUnit.SECONDS.sleep(5)

      val news = NewsResult(source.toString, name, 0,
        Seq(
          NewsItem("测试", "http://hostname.com/news/1", source.toString, "2015-10-23 22:22:22", "这里是内部摘要", ""),
          NewsItem("测试", "http://hostname.com/news/2", source.toString, "2015-10-23 23:22:22", "这里是内部摘要", ""),
          NewsItem("测试", "http://hostname.com/news/3", source.toString, "2015-10-24 00:22:22", "这里是内部摘要", "")
        ))
      doSender ! SearchResult(news)
  }
}

object SearchPageWorker {
  def props(name: String, source: NewsSource.Value) = Props(new SearchPageWorker(name, source))
}

class ItemPageWorker(item: NewsItem) extends MetricActor {
  override val metricReceive: Actor.Receive = {
    case StartFetchItemPage =>
      val doSender = sender()

      // TODO sleep 5s 模拟抓取新闻详情内容
      TimeUnit.SECONDS.sleep(5)

      val newsItem = item.copy(content = "这是新闻详情内容")
      doSender ! ItemPageResult(newsItem)
  }
}

object ItemPageWorker {
  def props(item: NewsItem) = Props(new ItemPageWorker(item))
}
