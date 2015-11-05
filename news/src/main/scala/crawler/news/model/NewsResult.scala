package crawler.news.model

import crawler.news.NewsSource

/**
 * 新闻结果
 * Created by yangjing on 15-11-3.
 */
case class NewsResult(source: NewsSource.Value, key: String, count: Int, news: Seq[NewsItem], error: Option[String] = None)
