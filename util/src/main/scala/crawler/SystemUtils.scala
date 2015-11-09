package crawler

import java.nio.charset.Charset

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import crawler.util.http.HttpClient

/**
 * System Utils
 * Created by yangjing on 15-11-5.
 */
object SystemUtils {
  val crawlerConfig = ConfigFactory.load().getConfig("crawler")

  val DEFAULT_CHARSET = Charset.forName("UTF-8")

  implicit val system = ActorSystem(crawlerConfig.getString("akka-system-name"))
  implicit val materializer = ActorMaterializer()

  val httpClient = HttpClient(crawlerConfig.getConfig("http-client"))
}
