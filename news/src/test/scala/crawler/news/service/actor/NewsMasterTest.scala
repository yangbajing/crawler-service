package crawler.news.service.actor

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.util.Timeout
import crawler.news.commands.RequestSearchNews
import crawler.news.{NewsSource, NewsUtils, SearchMethod}
import crawler.testsuite.ServiceSpec

import scala.concurrent.duration._

/**
 * NewsMasterTest
 * Created by yangjing on 15-11-5.
 */
class NewsMasterTest extends ServiceSpec {

  import system.dispatcher

  implicit val timeout = Timeout(60.seconds)

  "NewsMasterTest" should {
    "news-master" in {
      val newsMaster = system.actorOf(NewsMaster.props, "news")

      val f = (newsMaster ? RequestSearchNews("杭州誉存科技有限公司", Seq(NewsSource.BAIDU), SearchMethod.F, 3.seconds)).mapTo[NewsUtils.NewsResultType]

      f onSuccess {
        case Right(news) =>
          println("Success: " + news)
          news.news.size mustBe 3
        case Left(e) =>
          println("Success: " + e)
      }

      f onFailure { case e =>
        println("Failure: " + e)
      }

      TimeUnit.SECONDS.sleep(20)
    }
  }
}
