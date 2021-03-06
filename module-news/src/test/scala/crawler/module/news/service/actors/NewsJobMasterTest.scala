package crawler.module.news.service.actors

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.util.Timeout
import crawler.SystemUtils
import crawler.module.news.commands.{SearchNews, RequestSearchNews}
import crawler.module.news.crawlers.{BaiduNews, NewsCrawler}
import crawler.module.news.enums.{SearchMethod, ItemSource}
import crawler.module.news.model.SearchResult
import crawler.module.news.service.NewsMaster
import crawler.testsuite.ServiceSpec

import scala.concurrent.duration._

/**
  * NewsMasterTest
  * Created by yangjing on 15-11-5.
  */
class NewsJobMasterTest extends ServiceSpec {

  implicit val timeout = Timeout(60.seconds)

  "NewsMasterTest" should {
    NewsCrawler.registerCrawler(ItemSource.baidu, new BaiduNews(SystemUtils.httpClient))

    "news-master" in {
      val sources = Seq(ItemSource.baidu)
      val newsMaster = system.actorOf(NewsMaster.props, NewsMaster.actorName)
      val msg = RequestSearchNews(sources, SearchNews("杭州誉存科技有限公司", SearchMethod.F, 3.seconds))

      val f = (newsMaster ? msg).mapTo[Seq[SearchResult]]

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
