package crawler.news.service

import akka.pattern.ask
import crawler.news.commands.RequestSearchNews
import crawler.news.model.NewsResult
import crawler.news.service.actor.NewsMaster
import crawler.news.{NewsSource, NewsUtils, SearchMethod}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * 新闻服务
 * Created by yangjing on 15-11-3.
 */
class NewsService() {

  import crawler.SystemUtils._
  import system.dispatcher

  val newsMaster = system.actorOf(NewsMaster.props, "news-master")

  def fetchNews(company: String,
                sources: Seq[String],
                method: SearchMethod.Value,
                duration: FiniteDuration): Future[NewsResult] = {
    val ss = sources.collect { case s if NewsSource.values.exists(_.toString == s) => NewsSource.withName(s) }
    val msg = RequestSearchNews(company, ss, method, duration)

    newsMaster.ask(msg)(duration + 5.seconds).mapTo[NewsUtils.NewsResultType].flatMap {
      case Right(result) =>
        Future.successful(result)
      case Left(e) =>
        Future.failed(e)
    }
  }

}
