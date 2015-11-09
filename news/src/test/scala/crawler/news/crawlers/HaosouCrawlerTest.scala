package crawler.news.crawlers

import akka.util.Timeout
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by yangjing on 15-11-9.
 */
class HaosouCrawlerTest extends ServiceSpec {

  import system.dispatcher

  implicit val timeout = Timeout(30.seconds)


  "HaosouCrawlerTest" should {

    "fetchNewsList" in {
      val haosou = new HaosouCrawler(HttpClient())
      val result = Await.result(haosou.fetchNewsList("誉存科技"), timeout.duration)
      result.news.foreach(println)
      println(result.source + " " + result.key)
      result.news must not be empty
    }

  }

  override implicit def patienceConfig: PatienceConfig = super.patienceConfig
}
