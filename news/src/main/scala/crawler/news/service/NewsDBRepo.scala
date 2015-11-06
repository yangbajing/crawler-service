package crawler.news.service

import com.datastax.driver.core.{Session, PreparedStatement, UDTValue}
import crawler.SystemUtils
import crawler.news.enums.{NewsSource, SearchMethod}
import crawler.news.model.{NewsResult, NewsItem}
import crawler.util.persist.CassandraPersists
import crawler.util.time.DateTimeUtils

import scala.collection.mutable

/**
 * News DB Service
 * Created by yangjing on 15-11-6.
 */
class NewsDBRepo {

  import scala.collection.JavaConverters._

  val keyspace = SystemUtils.crawlerConfig.getString("cassandra.keyspace")
  val cachePrepares = mutable.Map.empty[String, PreparedStatement]

  def findNews(key: String,
               sources: Seq[NewsSource.Value],
               method: SearchMethod.Value) = {
    require(sources.nonEmpty, "source参数不能为空")
    CassandraPersists.using(keyspace) { session =>
      //      val stmt = session.prepare("SELECT * FROM search_page WHERE key = ? AND source in ? allow filtering")
      //      val rs = session.execute(stmt.bind(key, sources.map(_.toString).asJava))

      val selectInTuple = sources.map(_ => "").mkString("(source = ?", " or source = ?", ")")
      val stmt = getPreparedStatement(session, s"SELECT * FROM search_page WHERE key = ? AND $selectInTuple allow filtering")
      val params = key +: sources.map(_.toString)

      session.execute(stmt.bind(params: _*)).asScala.map { row =>
        val news = row.getList("news", classOf[UDTValue]).asScala.map(udt =>
          NewsItem(
            udt.getString("title"),
            udt.getString("url"),
            udt.getString("source"),
            DateTimeUtils.toLocalDateTime(udt.getDate("datetime")),
            udt.getString("summary"),
            "")
        )

        NewsResult(
          NewsSource.withName(row.getString("source")),
          row.getString("key"),
          row.getInt("count"),
          news)
      }.toList
    }
  }

  def saveToNewsPages(news: Traversable[NewsItem]) =
    CassandraPersists.using(keyspace) { session =>
      val stmt = getPreparedStatement(session,
        "INSERT INTO news_page(url, source, title, datetime, summary, content) VALUES(?, ?, ?, ?, ?, ?)")
      news.map(item =>
        session.executeAsync(stmt.bind(
          item.url,
          item.source,
          item.title,
          DateTimeUtils.toDate(item.datetime),
          item.summary,
          item.content))
      )
    }

  def saveToSearchPage(newsResult: NewsResult) =
    CassandraPersists.using(keyspace) { session =>
      val newsType = CassandraPersists.userType(keyspace, "news_type")
      val stmt = getPreparedStatement(session, "INSERT INTO search_page(key, source, count, news) VALUES(?, ?, ?, ?)")
      session.executeAsync(stmt.bind(
        newsResult.key,
        newsResult.source.toString,
        Integer.valueOf(newsResult.count),
        newsResult.news.map(n => NewsItem.toUDTValue(newsType, n)).asJava))
    }

  private def getPreparedStatement(session: Session, sql: String): PreparedStatement = {
    cachePrepares.getOrElse(sql, {
      val p = session.prepare(sql)
      cachePrepares.put(sql, p)
      p
    })
  }

}
