package crawler.news.model

/**
 * Created by yangjing on 15-11-3.
 */
case class NewsResult(source: String, company: String, count: Int, news: Seq[NewsItem])
