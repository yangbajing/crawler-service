package crawler.news.model

import java.time.LocalDateTime

import crawler.news.enums.NewsSource

/**
 * 新闻结果
 * Created by yangjing on 15-11-3.
 */
case class NewsResult(source: NewsSource.Value,
                      key: String,
                      datetime: LocalDateTime,
                      count: Int,
                      news: Seq[NewsItem],
                      error: Option[String] = None)
