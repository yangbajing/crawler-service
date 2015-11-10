package crawler.news.crawlers

import akka.util.Timeout
import crawler.testsuite.ServiceSpec
import crawler.util.http.HttpClient
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Wechat News Test
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-10.
 */
class WechatNewsTest extends ServiceSpec {

  import system.dispatcher

  implicit val timeout = Timeout(30.seconds)
  "WechatNewsTest" should {

    "fetchNewsList" in {
      val wechat = new WechatNews(HttpClient())
      val f = wechat.fetchNewsList("成都念念科技有限公司")
      val result = Await.result(f, timeout.duration)
      result.news.foreach(println)
      println(result.count + " " + result.key)
      result.news must not be empty
    }

  }

}
