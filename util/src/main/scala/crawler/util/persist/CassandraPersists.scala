package crawler.util.persist

import com.datastax.driver.core.{Cluster, Session, UserType}
import crawler.SystemUtils

import scala.collection.JavaConverters._

/**
 * CassandraPersists
 * Created by yangjing on 15-11-6.
 */
object CassandraPersists {

  val cluster =
    Cluster.builder().addContactPoints(SystemUtils.crawlerConfig.getStringList("cassandra.nodes").asScala: _*).build()

  def userType(keyspace: String, userType: String): UserType =
    cluster.getMetadata.getKeyspace(keyspace).getUserType(userType)

  def using[R](keyspace: String)(func: Session => R): R = {
    val session = cluster.connect(keyspace)
    try {
      func(session)
    } finally {
      session.closeAsync()
    }
  }
}
