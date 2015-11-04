package crawler.news.service.actor

import akka.pattern.ask
import akka.util.Timeout
import crawler.news.model.NewsResult
import crawler.news.{NewsSource, SearchMethod}
import crawler.news.commands.SearchNews
import crawler.testsuite.ServiceSpec

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * 新闻Actor测试
 * Created by yangjing on 15-11-4.
 */
class NewsJobTest extends ServiceSpec {
  implicit val timeout = Timeout(15.seconds)
  "NewsJobTest" should {
    "news-job" in {
      val newsJob = system.actorOf(NewsJob.props(SearchMethod.A), "news-job")

      val f = (newsJob ? SearchNews("杭州誉存科技有限公司", NewsSource.BAIDU)).mapTo[NewsResult]
      val newsResult = Await.result(f, timeout.duration)
      println(newsResult)
      newsResult.count mustBe 0
    }
  }
}
