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
                    author: String,
                    datetime: LocalDateTime,
                    // 摘要
                    summary: String,
                    content: Option[String] = None,
                    error: Option[String] = None)

object NewsItem {
  def toUDTValue(userType: UserType, ni: NewsItem): UDTValue = {
    userType.newValue()
      .setString("title", ni.title)
      .setString("url", ni.url)
      .setString("author", ni.author)
      .setDate("datetime", DateTimeUtils.toDate(ni.datetime))
      .setString("summary", ni.summary)
  }
}
