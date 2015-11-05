package crawler.news.service.actor

import akka.actor.{ActorRef, Props}
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, NewsUtils}
import crawler.news.commands.{RequestSearchNews, SearchNews}
import crawler.util.actors.MetricActor

/**
 * NewsJob Master
 * 成功返回: Seq[NewsResult]
 * Created by yangjing on 15-11-5.
 */
class NewsJobMaster(sources: Seq[NewsSource.Value]) extends MetricActor {
  @volatile var _completeJobs = 0
  @volatile var _newsResults = List.empty[NewsResult]
  @volatile var _doSender: ActorRef = null

  override val metricReceive: Receive = {
    case RequestSearchNews(company, method, duration) =>
      // TODO 只执行百度
      _doSender = sender()

      sources.foreach { source =>
        val jobName = "job-" + NewsUtils.getIndent
        val jobActor = context.actorOf(NewsJob.props(source, method, duration), jobName)
        jobActor ! SearchNews(company, self)
      }

    //      val source = sources.head
    //      val jobName = "job-" + NewsUtils.getIndent
    //      val jobActor = context.actorOf(NewsJob.props(source, method, duration), jobName)
    //      jobActor ! SearchNews(company, doSender)

    case result: NewsResult =>
      _completeJobs += 1
      _newsResults ::= result
      if (sources.size == _completeJobs) {
        _doSender ! _newsResults
      }

  }
}

object NewsJobMaster {
  def props(sources: Seq[NewsSource.Value]) = Props(new NewsJobMaster(sources))
}
