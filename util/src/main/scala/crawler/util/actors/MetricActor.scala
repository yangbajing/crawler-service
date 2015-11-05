package crawler.util.actors

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

/**
 * Metric Actor
 * Created by yangjing on 15-11-4.
 */
trait MetricActor extends Actor with LazyLogging {
  override def preStart(): Unit = {
    logger.debug(s"${self.path} preStart")
  }

  override def postStop(): Unit = {
    logger.debug(s"${self.path} postStop")
  }


  final override def receive: Receive = {
    case s =>
      if (metricReceive.isDefinedAt(s)) {
        logger.debug(s"${self.path} receive message: $s")
        metricReceive(s)
      } else {
        logger.warn(s"${self.path} receive message: $s")
      }
  }

  val metricReceive: Receive

}
