package crawler.util.http

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

/**
 * Akka Http Json Supoort
 * Created by yangjing on 15-11-5.
 */
trait JsonSupport extends Json4sSupport {
  implicit val formats = DefaultFormats
  implicit val serialization = Serialization
}

object JsonSupport extends JsonSupport
