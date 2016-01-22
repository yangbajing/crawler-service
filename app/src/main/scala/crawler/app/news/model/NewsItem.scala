package crawler.app.news.model

import java.time.LocalDateTime

import com.datastax.driver.core.{UDTValue, UserType}
import crawler.common.JsonSupport
import crawler.util.time.TimeUtils
import org.json4s.Extraction

/**
  * 新闻详情
  * Created by yangjing on 15-11-3.
  */
case class NewsItem(title: String,
                    url: String,
                    // 新闻来源（站点）
                    source: String,
                    time: Option[LocalDateTime],
                    // 摘要
                    `abstract`: String,
                    content: Option[String] = None,
                    values: Seq[String] = Nil,
                    error: Option[String] = None) {
  def jsonPretty = {
    import JsonSupport._
    val jv = Extraction.decompose(this)
    serialization.writePretty(jv)
  }
}

object NewsItem {
  def toUDTValue(userType: UserType, ni: NewsItem): UDTValue = {
    userType.newValue()
      .setString("title", ni.title)
      .setString("url", ni.url)
      .setString("source", ni.source)
      .setTimestamp("time", ni.time.map(TimeUtils.toDate).orNull)
      .setString("abstract", ni.`abstract`)
  }
}
