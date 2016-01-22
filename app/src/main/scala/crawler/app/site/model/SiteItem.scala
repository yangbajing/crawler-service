package crawler.app.site.model

import java.time.LocalDateTime

import crawler.common.JsonSupport
import org.json4s.Extraction

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-22.
  */
case class SiteItem(title: String,
                    url: String,
                    // 新闻来源（站点）
                    source: String,
                    time: Option[LocalDateTime],
                    // 摘要
                    `abstract`: String,
                    values: Seq[String] = Nil) {
  def jsonPretty = {
    import JsonSupport._
    val jv = Extraction.decompose(this)
    serialization.writePretty(jv)
  }
}
