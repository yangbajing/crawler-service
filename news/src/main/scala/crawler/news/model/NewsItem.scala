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
                    datetime: LocalDateTime,
                    // 摘要
                    summary: String,
                    content: String,
                    error: Option[String] = None)

object NewsItem {
  def toUDTValue(userType: UserType, ni: NewsItem): UDTValue = {
    userType.newValue()
      .setString("title", ni.title)
      .setString("url", ni.url)
      .setString("source", ni.source)
      .setDate("datetime", DateTimeUtils.toDate(ni.datetime))
      .setString("summary", ni.summary)
  }
}
