package crawler.module.news

import crawler.module.news.enums.{SearchMethod, ItemSource}
import crawler.util.http.TJsonSupport
import org.json4s.Formats
import org.json4s.ext.EnumNameSerializer

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-22.
  */
trait NewsJsonSupport extends TJsonSupport {
  implicit val formats: Formats = defaultFormats +
    new EnumNameSerializer(ItemSource) +
    new EnumNameSerializer(SearchMethod)
}

object NewsJsonSupport extends NewsJsonSupport
