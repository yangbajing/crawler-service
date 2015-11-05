package crawler.util.actors

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

/**
 * Metric Actor
 * Created by yangjing on 15-11-4.
 */
trait MetricActor extends Actor with LazyLogging {
  final override def preStart(): Unit = {
    logger.debug(s"${self.path} preStart")
    MetricActor.incrementActorSize()
    metricPreStart()
  }

  final override def postStop(): Unit = {
    metricPostStop()
    MetricActor.decrementActorSize()
    logger.debug(s"${self.path} postStop")
  }

  final override def receive: Receive = {
    case s =>
      if (metricReceive.isDefinedAt(s)) {
        logger.debug(s"${self.path} receive message: $s")
        metricReceive(s)
      } else {
        logger.warn(s"${self.path} receive message: $s")
        unhandled(s)
      }
  }

  def metricPreStart(): Unit = {
  }

  def metricPostStop(): Unit = {
  }

  val metricReceive: Receive

}

object MetricActor {
  private val _currentActiveActors = new AtomicInteger(0)

  def incrementActorSize() = _currentActiveActors.incrementAndGet()

  def decrementActorSize() = _currentActiveActors.decrementAndGet()

  def currentActorSize() = _currentActiveActors.get()
}
