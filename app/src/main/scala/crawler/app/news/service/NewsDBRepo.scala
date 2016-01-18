package crawler.app.news.service

import java.time.LocalDateTime

import com.datastax.driver.core.{PreparedStatement, Session, UDTValue}
import com.typesafe.scalalogging.LazyLogging
import crawler.SystemUtils
import crawler.enums.{ItemSource, SearchMethod}
import crawler.model.{NewsItem, NewsPage, SearchResult}
import crawler.util.persist.CassandraPersists
import crawler.util.time.TimeUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * News DB Service
 * Created by yangjing on 15-11-6.
 */
class NewsDBRepo extends LazyLogging {

  val KEYSPACE = SystemUtils.crawlerConfig.getString("cassandra.keyspace")
  val cachePrepares = mutable.Map.empty[String, PreparedStatement]

  private def findNews(key: String,
                       source: ItemSource.Value,
                       method: SearchMethod.Value,
                       time: LocalDateTime)(
                        implicit ec: ExecutionContextExecutor
                        ): Future[Seq[SearchResult]] = {

    logger.debug(s"key: $key, source: $source, method: $method, time: $time")

    CassandraPersists.using(KEYSPACE) { implicit session =>
      val stmt = getPreparedStatement(session, "SELECT * FROM search_page WHERE key = ? AND source = ? AND time > ?")
      val futureResultSet = session.executeAsync(stmt.bind(key, source.toString, TimeUtils.toDate(time)))
      val list = CassandraPersists.execute(futureResultSet) { rs =>
        rs.asScala.map { row =>
          val news = row.getList("news", classOf[UDTValue]).asScala.map(udt =>
            NewsItem(
              udt.getString("title"),
              udt.getString("url"),
              udt.getString("source"),
              TimeUtils.toLocalDateTime(udt.getTimestamp("time")),
              udt.getString("abstract"))
          )

          val newsItemFuture = Future.sequence(news.map(news =>
            findOneNewsPageItem(news.url).map(nop => news.copy(content = nop.map(_.content)))))

          newsItemFuture.map { newsList =>
            SearchResult(
              ItemSource.withName(row.getString("source")),
              row.getString("key"),
              TimeUtils.toLocalDateTime(row.getTimestamp("time")),
              row.getInt("count"),
              newsList)
          }
        }.toList
      }

      list.flatMap(futures => Future.sequence(futures))
    }
  }

  def findNews(key: String,
               sources: Traversable[ItemSource.Value],
               method: SearchMethod.Value,
               time: Option[LocalDateTime])(
                implicit ec: ExecutionContextExecutor
                ): Future[List[SearchResult]] = {

    val futureList = CassandraPersists.using(KEYSPACE) { implicit session =>
      val pstmt =
        if (time.isEmpty) getPreparedStatement(session, "SELECT * FROM search_page WHERE key = ? AND source = ?")
        else getPreparedStatement(session, "SELECT * FROM search_page WHERE key = ? AND source = ? AND time > ?")

      sources.flatMap { source =>
        val stmt =
          if (time.isEmpty) pstmt.bind(key, source.toString)
          else pstmt.bind(key, source.toString, TimeUtils.toDate(time.get))

        session.execute(stmt).asScala.map { row =>
          val news = row.getList("news", classOf[UDTValue]).asScala.map(udt =>
            NewsItem(
              udt.getString("title"),
              udt.getString("url"),
              udt.getString("source"),
              TimeUtils.toLocalDateTime(udt.getTimestamp("time")),
              udt.getString("abstract"))
          )

          val newsItemFuture = Future.sequence(news.map(news =>
            findOneNewsPageItem(news.url).map(nop => news.copy(content = nop.map(_.content)))))

          newsItemFuture.map(list =>
            SearchResult(
              ItemSource.withName(row.getString("source")),
              row.getString("key"),
              TimeUtils.toLocalDateTime(row.getTimestamp("time")),
              row.getInt("count"),
              list)
          )

        }
      }.toList

    }

    Future.sequence(futureList)
  }

  def findOneNewsPageItem(url: String)(
    implicit session: Session, ec: ExecutionContextExecutor
    ): Future[Option[NewsPage]] = {

    val stmt = getPreparedStatement(session, "SELECT * FROM news_page WHERE url = ?")
    CassandraPersists.execute(session.executeAsync(stmt.bind(url))) { rs =>
      rs.one match {
        case null =>
          None
        case row =>
          Some(NewsPage(
            row.getString("url"),
            row.getString("title"),
            row.getString("source"),
            TimeUtils.toLocalDateTime(row.getTimestamp("time")),
            row.getString("abstract"),
            row.getString("content"))
          )
      }
    }
  }

  def saveToNewsPage(page: NewsPage): Unit = {
    CassandraPersists.using(KEYSPACE) { session =>
      val stmt = getPreparedStatement(session,
        "INSERT INTO news_page(url, title, source, time, abstract, content) VALUES(?, ?, ?, ?, ?, ?)")
      session.executeAsync(stmt.bind(
        page.url,
        page.title,
        page.source,
        TimeUtils.toDate(page.time),
        page.`abstract`,
        page.content))
    }
  }

  def saveToSearchPage(newsResult: SearchResult) = {
//    logger.debug(newsResult.news.mkString("\n"))
    logger.info(s"key: ${newsResult.key} found news: ${newsResult.count}, saved: ${newsResult.news.size}")
    CassandraPersists.using(KEYSPACE) { session =>
      val newsType = CassandraPersists.userType(KEYSPACE, "news_type")
      val stmt = getPreparedStatement(session, "INSERT INTO search_page(key, source, time, count, news) VALUES(?, ?, ?, ?, ?)")
      session.executeAsync(stmt.bind(
        newsResult.key,
        newsResult.source.toString,
        TimeUtils.toDate(newsResult.time),
        Integer.valueOf(newsResult.count),
        newsResult.news.map(n => NewsItem.toUDTValue(newsType, n)).asJava))
    }
  }

  private def getPreparedStatement(session: Session, sql: String): PreparedStatement = {
    //    println("sql: " + sql)
    cachePrepares.getOrElse(sql, {
      val p = session.prepare(sql)
      cachePrepares.put(sql, p)
      p
    })
  }

}
