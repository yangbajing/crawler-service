package crawler.util.http

import java.time.LocalDateTime

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpCharsets, MediaTypes}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import crawler.util.time.TimeUtils
import org.json4s._
import org.json4s.jackson.Serialization

/**
  * Akka Http Json Supoort
  * Created by yangjing on 15-11-5.
  */
trait TJsonSupport {
  def defaultFormats: Formats = DefaultFormats + new LocalDateTimeSerializer()

  implicit val serialization = Serialization
  implicit val formats: Formats

}

object TJsonSupport extends TJsonSupport {
  override implicit val formats: Formats = defaultFormats
}

class LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format =>
  ( {
    case JString(s) => LocalDateTime.parse(s, TimeUtils.formatterDateTime)
    case JNull => null
  }, {
    case d: LocalDateTime => JString(TimeUtils.formatterDateTime.format(d))
  })
)

