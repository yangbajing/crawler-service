package crawler.util.http

import java.time.LocalDateTime

import crawler.util.time.DateTimeUtils
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s._
import org.json4s.jackson.Serialization

/**
 * Akka Http Json Supoort
 * Created by yangjing on 15-11-5.
 */
trait TJsonSupport extends Json4sSupport {
  def defaultFormats: Formats = DefaultFormats + new LocalDateTimeSerializer()

  implicit val serialization = Serialization
  implicit val formats: Formats
}

class LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format =>
  ( {
    case JString(s) => LocalDateTime.parse(s, DateTimeUtils.formatterDateTime)
    case JNull => null
  }, {
    case d: LocalDateTime => JString(DateTimeUtils.formatterDateTime.format(d))
  })
)

