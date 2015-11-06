package crawler.util.persist

import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection}

/**
 * Mongodb持久化工具
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
object MongodbPersists {
  val client = MongoClient("mongodb://localhost")

  def getDatabase(name: String) = client.getDatabase(name)

  def save[R](dbName: String, collName: String)(func: MongoCollection[Document] => R): R = {
    val coll = getDatabase(dbName).getCollection[Document](collName)
    func(coll)
  }
}
