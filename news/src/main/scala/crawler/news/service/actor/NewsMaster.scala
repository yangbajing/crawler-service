package crawler.news.service.actor

import akka.actor.Props
import crawler.news.commands.{RequestSearchNews, SearchNews}
import crawler.news.{NewsSource, NewsUtils}
import crawler.util.actors.MetricActor

/**
 * News Master
 * Created by yangjing on 15-11-5.
 */
class NewsMaster extends MetricActor {
  override val metricReceive: Receive = {
    case RequestSearchNews(company, sources, method, duration) =>
      // TODO 只执行百度
      val doSender = sender()
      val jobName = "job-" + NewsUtils.getInt
      val jobActor = context.actorOf(NewsJob.props(method, duration), jobName)
      jobActor ! SearchNews(company, NewsSource.BAIDU, doSender)

  }
}

object NewsMaster {
  def props = Props(new NewsMaster)
}
