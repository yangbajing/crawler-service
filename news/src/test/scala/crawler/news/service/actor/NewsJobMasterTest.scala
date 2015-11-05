package crawler.news.service.actor

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.util.Timeout
import crawler.SystemUtils
import crawler.news.commands.RequestSearchNews
import crawler.news.crawlers.{BaiduCrawler, NewsCrawler}
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, SearchMethod}
import crawler.testsuite.ServiceSpec

import scala.concurrent.duration._

/**
 * NewsMasterTest
 * Created by yangjing on 15-11-5.
 */
class NewsJobMasterTest extends ServiceSpec {

  import system.dispatcher

  implicit val timeout = Timeout(60.seconds)

  "NewsMasterTest" should {
    NewsCrawler.registerCrawler(NewsSource.BAIDU, new BaiduCrawler(SystemUtils.httpClient))

    "news-master" in {
      val sources = Seq(NewsSource.BAIDU)
      val newsMaster = system.actorOf(NewsJobMaster.props(sources), "news")

      val f = (newsMaster ? RequestSearchNews("杭州誉存科技有限公司", SearchMethod.F, 3.seconds)).mapTo[Seq[NewsResult]]

      f onSuccess { case list =>
        list.foreach(println)
        list.size mustBe 1
      }

      f onFailure { case e =>
        println("Failure: " + e)
      }

      TimeUnit.SECONDS.sleep(20)
    }
  }
}
