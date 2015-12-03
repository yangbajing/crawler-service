package crawler.util.http

import java.time.LocalDateTime

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ContentTypes, HttpCharsets, MediaTypes}
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


  implicit def json4sUnmarshallerConverter[A: Manifest](serialization: Serialization, formats: Formats)(implicit mat: Materializer): FromEntityUnmarshaller[A] =
    json4sUnmarshaller(manifest, serialization, formats, mat)

  implicit def json4sUnmarshaller[A: Manifest](implicit serialization: Serialization, formats: Formats, mat: Materializer): FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        serialization.read(input)
      }

  implicit def json4sMarshallerConverter[A <: AnyRef](serialization: Serialization, formats: Formats): ToEntityMarshaller[A] =
    json4sMarshaller(serialization, formats)

  implicit def json4sMarshaller[A <: AnyRef](implicit serialization: Serialization, formats: Formats): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`)(serialization.write[A])
}

class LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format =>
  ( {
    case JString(s) => LocalDateTime.parse(s, TimeUtils.formatterDateTime)
    case JNull => null
  }, {
    case d: LocalDateTime => JString(TimeUtils.formatterDateTime.format(d))
  })
)

