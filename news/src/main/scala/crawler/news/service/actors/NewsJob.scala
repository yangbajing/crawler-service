package crawler.news.service.actors

import akka.actor.{ActorRef, PoisonPill, Props}
import crawler.news.commands.{SearchNews, StartSearchNews}
import crawler.news.enums.NewsSource
import crawler.news.model.NewsResult
import crawler.util.actors.MetricActor

/**
 * NewsJob
 * 成功返回: Seq[NewsResult]
 * Created by yangjing on 15-11-5.
 */
class NewsJob(sources: Seq[NewsSource.Value], reqSender: ActorRef) extends MetricActor {
  @volatile var _completeJobs = 0
  @volatile var _newsResults = List.empty[NewsResult]

  override val metricReceive: Receive = {
    case SearchNews(key, method, duration) =>
      sources.foreach { source =>
        val jobName = source.toString
        val jobActor = context.actorOf(NewsSourceJob.props(source, method, key, duration, self), jobName)
        jobActor ! StartSearchNews
      }

    case result: NewsResult =>
      _completeJobs += 1
      _newsResults ::= result
      if (sources.size == _completeJobs) {
        reqSender ! _newsResults

        // TODO 把 NewsJob 内的超时判断上移到 NewsJob ?
        self ! PoisonPill
      }

  }
}

object NewsJob {
  def props(sources: Seq[NewsSource.Value], reqSender: ActorRef) = Props(new NewsJob(sources, reqSender))
}
