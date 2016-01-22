package crawler.module.news.service.actors

import akka.actor.{ActorRef, PoisonPill, Props}
import crawler.module.news.commands.{SearchNews, StartSearchNews}
import crawler.module.news.enums.ItemSource
import crawler.module.news.model.SearchResult
import crawler.util.actors.MetricActor

/**
 * NewsJob
 * 成功返回: Seq[NewsResult]
 * Created by yangjing on 15-11-5.
 */
class NewsJob(sources: Seq[ItemSource.Value], reqSender: ActorRef) extends MetricActor {
  @volatile var _completeJobs = 0
  @volatile var _newsResults = List.empty[SearchResult]

  override val metricReceive: Receive = {
    case SearchNews(key, method, duration) =>
      sources.foreach { source =>
        val jobName = source.toString
        val jobActor = context.actorOf(NewsSourceJob.props(source, method, key, duration, self), jobName)
        jobActor ! StartSearchNews
      }

    case result: SearchResult =>
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
  def props(sources: Seq[ItemSource.Value], reqSender: ActorRef) = Props(new NewsJob(sources, reqSender))
}
