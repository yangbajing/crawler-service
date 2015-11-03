package crawler.news.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
class BaiduNews {

}

object BaiduNews {
  val BAIDU_NEWS_BASE_URL = "http://news.baidu.com/ns?word=%s&tn=news&from=news&cl=2&rn=20&ct=1"
  val TIME_PATTERN = """\d{4}年\d{2}月\d{2}日 \d{2}:\d{2}""".r

  def main(args: Array): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val f = Http().singleRequest(HttpRequest(uri = BAIDU_NEWS_BASE_URL.format("杭州誉存科技有限公司")))
    f.foreach { resp =>
      println(resp.entity)
    }

    system.shutdown()
    system.awaitTermination()
  }
}
