package crawler.common

import crawler.enums.{ItemSource, QueryCond, SearchMethod, SearchSyntax}
import crawler.util.http.TJsonSupport
import org.json4s.Formats
import org.json4s.ext.EnumNameSerializer

/**
  * Json Support
  * Created by yangjing on 15-11-6.
  */
trait JsonSupport extends TJsonSupport {
  implicit val formats: Formats = defaultFormats +
    new EnumNameSerializer(ItemSource) +
    new EnumNameSerializer(SearchMethod) +
    new EnumNameSerializer(SearchSyntax) +
    new EnumNameSerializer(QueryCond)
}

object JsonSupport extends JsonSupport
