package crawler.app.common

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{HttpCharsets, MediaTypes}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import crawler.module.news.NewsJsonSupport
import crawler.module.site.QueryCond
import crawler.util.http.TJsonSupport
import org.json4s.ext.EnumNameSerializer
import org.json4s.{Formats, Serialization}

/**
  * Json Support
  * Created by yangjing on 15-11-6.
  */
trait JsonSupport extends TJsonSupport with NewsJsonSupport {
  implicit override val formats: Formats = defaultFormats +
    new EnumNameSerializer(QueryCond)

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
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(serialization.write[A])
}

object JsonSupport extends JsonSupport
