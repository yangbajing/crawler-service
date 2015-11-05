package crawler

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
 * System Utils
 * Created by yangjing on 15-11-5.
 */
object SystemUtils {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
}
