package crawler.util.persist

import java.util.Date

import crawler.SystemUtils
import org.scalatest.WordSpec

/**
 * Created by yangjing on 15-11-6.
 */
class CassandraPersistsTest extends WordSpec {

  "CassandraPersistsTest" should {

    "save" in {
      val keyspace = SystemUtils.crawlerConfig.getString("cassandra.keyspace")
      CassandraPersists.using(keyspace) { session =>
        val newsItem = Map(
          "url" -> "http://hostname/news/1.html",
          "source" -> "网易新闻",
          "title" -> "标题",
          "time" -> new Date(),
          "abstract" -> "新闻摘要")
        val bstmt = session.prepare("INSERT INTO search_page(source, key, count, news) VALUES(?, ?, ?, ?);")

        val newsTypeUDT = session.getCluster.getMetadata.getKeyspace(keyspace).getUserType("news_type")
        val nit = newsTypeUDT.newValue()
        newsItem.foreach {
          case ("time", value: Date) => nit.setTimestamp("time", value)
          case (key, value: String) => nit.setString(key, value)
        }

        val result = session.execute(bstmt.bind(
          "网易新闻",
          "杭州誉存科技有限公司",
          Integer.valueOf(2),
          java.util.Arrays.asList(nit)
        ))
        println(result)

      }
    }

  }
}
