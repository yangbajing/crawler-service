package crawler

import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ning.http.client.AsyncHttpClientConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import crawler.util.http.HttpClient

import scala.concurrent.duration._

/**
  * System Utils
  * Created by yangjing on 15-11-5.
  */
object SystemUtils extends StrictLogging {
  val crawlerConfig = ConfigFactory.load().getConfig("crawler")

  implicit val system = ActorSystem(crawlerConfig.getString("akka-system-name"))
  implicit val materializer = ActorMaterializer()

  val httpClient = {
    crawlerConfig.getConfig("http-client")
    val builder = new AsyncHttpClientConfig.Builder()
    builder.setMaxConnections(8192)
    builder.setMaxConnectionsPerHost(4)
    builder.setConnectTimeout(10 * 1000)
    builder.setPooledConnectionIdleTimeout(40 * 1000)
    builder.setRequestTimeout(90 * 1000)
    builder.setAllowPoolingConnections(true)
    builder.setFollowRedirect(true)
    HttpClient(builder.build(), Nil)
  }

  def shutdown(): Unit = {
    httpClient.close()
    system.shutdown()
    try {
      system.awaitTermination(5.seconds)
      System.exit(0)
    } catch {
      case e: TimeoutException =>
        logger.error(e.getLocalizedMessage, e)
        System.exit(3)
    }
  }

}
