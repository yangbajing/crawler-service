package crawler.news.crawlers

import akka.util.Timeout
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration._

class CourtNewsTest extends ServiceSpec {
  import system.dispatcher

  val timeout = Timeout(30.seconds)

  "CourtNewsTest" should {
    "fetchNewsList" in {
      val court = new CourtNews(HttpClient())
      val result = Await.result(court.fetchNewsList("重庆"), timeout.duration)
      result.news.foreach(println)
      println(result.key)
      result.news must not be empty
    }
  }
}
