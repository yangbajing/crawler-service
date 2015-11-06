package crawler.news.service.actors

import akka.actor.Props
import crawler.news.model.NewsResult
import crawler.util.actors.MetricActor
import crawler.util.http.JsonSupport
import crawler.util.persist.MongodbPersists
import org.mongodb.scala.bson.collection.immutable.Document

/**
 * 持久化
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-06.
 */
class PersistActor extends MetricActor {
  @volatile var _cacheNewsResult = List.empty[NewsResult]

  override def metricPostStop(): Unit = {
    persist()
  }

  override val metricReceive: Receive = {
    case newsResult: NewsResult =>
      _cacheNewsResult ::= newsResult
      if (_cacheNewsResult.size == PersistActor.BATCH_SIZE) {
        persist()
      }
  }

  /**
   * 主键设置:
   * key: 搜索页面以key + source + datetime 主键
   * item: 新闻详情页面以url + datetime 主键
   */
  def persist(): Unit = {
    // TODO 数据持久化。可将数据扔给MQ，由挂在MQ上的一个独立进程来执行数据持久化任务。

    if (_cacheNewsResult.nonEmpty) {
      // TODO 每个 NewsSource 单独存一个Collection?
      MongodbPersists.save("crawlers", "news") { coll =>
        import JsonSupport.formats

        // TODO NewsItem 需要单独存一个Collection吗?
        val docs = _cacheNewsResult.map(newsResult => Document(JsonSupport.serialization.write(newsResult)))
        coll.insertMany(docs)
        logger.info("已持久化 " + _cacheNewsResult.size + " 条数据")
      }
      _cacheNewsResult = Nil
    }
  }

}

object PersistActor {
  val BATCH_SIZE = 20
  val actorName = "persist"

  def props = Props(new PersistActor)
}
