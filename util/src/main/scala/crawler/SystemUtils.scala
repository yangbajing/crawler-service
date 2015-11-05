package crawler

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import crawler.util.http.HttpClient

/**
 * System Utils
 * Created by yangjing on 15-11-5.
 */
object SystemUtils {
  implicit val system = ActorSystem("crawler")
  implicit val materializer = ActorMaterializer()
  val httpClient = HttpClient()
}
