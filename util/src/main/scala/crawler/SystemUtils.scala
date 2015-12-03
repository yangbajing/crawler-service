package crawler

import java.nio.charset.Charset

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ning.http.client.AsyncHttpClientConfig
import com.typesafe.config.ConfigFactory
import crawler.util.http.HttpClient

import scala.concurrent.duration._

/**
 * System Utils
 * Created by yangjing on 15-11-5.
 */
object SystemUtils {
  val crawlerConfig = ConfigFactory.load().getConfig("crawler")

  val DEFAULT_CHARSET = Charset.forName("UTF-8")

  implicit val system = ActorSystem(crawlerConfig.getString("akka-system-name"))
  implicit val materializer = ActorMaterializer()

  val httpClient = {
    crawlerConfig.getConfig("http-client")
    val builder = new AsyncHttpClientConfig.Builder()
//    builder.setMaxConnections(40)
//    builder.setMaxConnectionsPerHost(20)
    builder.setConnectTimeout(10 * 1000)
    builder.setPooledConnectionIdleTimeout(40 * 1000)
    builder.setRequestTimeout(90 * 1000)
    builder.setAllowPoolingConnections(true)
    builder.setFollowRedirect(true)
    HttpClient(builder.build(), Nil)
  }

  def shutdown(): Unit = {
    system.shutdown()
    system.awaitTermination(5.seconds)
  }
}
