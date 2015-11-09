package crawler.news.model

import java.time.LocalDateTime

/**
 * 新闻页
 * Created by yangjing on 15-11-9.
 */
case class NewsPage(url: String,
                    title: String,
                    author: String,
                    datetime: LocalDateTime,
                    src: String,
                    summary: String,
                    content: String)
