package crawler.news

import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.util.http.TJsonSupport
import org.json4s.Formats
import org.json4s.ext.EnumNameSerializer

/**
 * Json Support
 * Created by yangjing on 15-11-6.
 */
object JsonSupport extends TJsonSupport {
  implicit val formats: Formats = defaultFormats +
    new EnumNameSerializer(NewsSource) +
    new EnumNameSerializer(SearchMethod)
}
