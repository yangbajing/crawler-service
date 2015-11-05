package crawler.news.service.actor

import akka.actor.{ActorRef, Cancellable, PoisonPill, Props}
import akka.pattern.AskTimeoutException
import crawler.news.commands._
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, NewsUtils, SearchMethod}
import crawler.util.actors.MetricActor

import scala.concurrent.duration.FiniteDuration

/**
 * 新闻job
 * @param source 搜索源
 * @param method 搜索方式
 * @param duration 持续时间，到期后向未获取完新闻数据向客户端返回Timeout。children actor继续业务处理
 */
class NewsJob(source: NewsSource.Value, method: SearchMethod.Value, duration: FiniteDuration) extends MetricActor {
  @volatile var _reqSender: ActorRef = null
  @volatile var _newsResult: NewsResult = null
  @volatile var _isTimeout: Boolean = false
  @volatile var _notCompleteItemPageActorNames = Seq.empty[String]
  @volatile var _cancelableSchedule: Cancellable = _

  import context.dispatcher

  override def metricPreStart(): Unit = {
    // 定义超时时间
    _cancelableSchedule = context.system.scheduler.scheduleOnce(duration, self, SearchTimeout)
  }

  override def metricPostStop(): Unit = {
    if (!_cancelableSchedule.isCancelled) {
      _cancelableSchedule.cancel()
    }

    // TODO 数据持久化。可将数据扔给MQ，由挂在MQ上的一个独立进程来执行数据持久化任务。
    if (null != _newsResult && _newsResult.count > 0) {
      logger.info("已将数据传给MQ: " + _newsResult)
    } else {
      logger.warn("获取新闻失败: " + _newsResult.error)
    }
  }

  override val metricReceive: Receive = {
    case s@SearchNews(key, doSender) =>
      _reqSender = doSender
      val searchPage = context.actorOf(SearchPageWorker.props(source, key), "search-page")
      searchPage ! StartFetchSearchPage

    case SearchResult(newsResult) =>
      _newsResult = newsResult
      method match {
        case SearchMethod.F => // 需要抓取详情内容
          _notCompleteItemPageActorNames = newsResult.news.zipWithIndex.map { case (item, idx) =>
            val childName = "item-" + idx
            val itemPage = context.actorOf(ItemPageWorker.props(source, item), childName)
            itemPage ! StartFetchItemPage
            childName
          }

        case SearchMethod.S => // 只抓取摘要
          if (!_isTimeout) {
            _reqSender ! _newsResult
          }
          self ! PoisonPill
      }

    case ItemPageResult(newItem) =>
      val doSender = sender()
      _notCompleteItemPageActorNames = _notCompleteItemPageActorNames.filterNot(_ == doSender.path.name)

      // 更新 result.news
      val news = _newsResult.news.map(oldItem => if (oldItem.url == newItem.url) newItem else oldItem)
      _newsResult = _newsResult.copy(news = news)

      if (_notCompleteItemPageActorNames.isEmpty) {
        if (!_isTimeout) {
          _reqSender ! _newsResult
        }

        self ! PoisonPill
      }

    case SearchTimeout =>
      _isTimeout = true

      // 此时向调用客户端返回超时，但实际的新闻抓取流程仍将继续
      _reqSender ! Left(new AskTimeoutException("搜索超时"))

    case SearchFailure(key, e) =>
      if (!_isTimeout) {
        _reqSender ! NewsResult(source, key, -1, Nil, Some(e.getLocalizedMessage))
      }
      self ! PoisonPill
  }

}

object NewsJob {
  def props(source: NewsSource.Value, method: SearchMethod.Value, duration: FiniteDuration) =
    Props(new NewsJob(source, method, duration))
}
