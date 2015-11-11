package crawler.news.model

import java.time.LocalDateTime

import com.datastax.driver.core.{UDTValue, UserType}
import crawler.util.time.DateTimeUtils

/**
 * 新闻详情
 * Created by yangjing on 15-11-3.
 */
case class NewsItem(title: String,
                    url: String,
                    // 新闻来源（站点）
                    source: String,
                    time: LocalDateTime,
                    // 摘要
                    `abstract`: String,
                    content: Option[String] = None,
                    error: Option[String] = None)

object NewsItem {
  def toUDTValue(userType: UserType, ni: NewsItem): UDTValue = {
    userType.newValue()
      .setString("title", ni.title)
      .setString("url", ni.url)
      .setString("source", ni.source)
      .setDate("time", DateTimeUtils.toDate(ni.time))
      .setString("abstract", ni.`abstract`)
  }
}
