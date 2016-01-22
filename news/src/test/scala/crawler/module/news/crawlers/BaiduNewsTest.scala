package crawler.module.news.crawlers

import akka.util.Timeout
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-12-03.
 */
class BaiduNewsTest extends ServiceSpec {

  implicit val timeout = Timeout(30.seconds)

  "BaiduNewsTest" should {

    "fetchNewsList" in {
      val baidu = new BaiduNews(HttpClient())
      val result = Await.result(baidu.fetchItemList("阿里巴巴"), timeout.duration)
      result.news.foreach(println)
      println(result.source + " " + result.key)
      println(result.news.size)
      result.news must not be empty
    }

  }
}
