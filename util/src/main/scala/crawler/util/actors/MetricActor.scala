package crawler.util.actors

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

/**
 * Metric Actor
 * Created by yangjing on 15-11-4.
 */
trait MetricActor extends Actor with LazyLogging {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    logger.debug(s"$self preStart")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.debug(s"$self postStop")
  }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
  }

  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
  }

  def metricReceive: Receive

  final override def receive: Receive = {
    case s if metricReceive.isDefinedAt(s) =>
      metricReceive(s)
  }

}
