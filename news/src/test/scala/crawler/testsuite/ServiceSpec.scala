package crawler.testsuite

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration.Duration

/**
 * Created by yangjing on 15-11-4.
 */
abstract class ServiceSpec
  extends WordSpec
  with BeforeAndAfterAll
  with MustMatchers
  with OptionValues
  with EitherValues
  with ScalaFutures {

  implicit val system = ActorSystem("service-spec")
  implicit val materializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination(Duration(10, TimeUnit.SECONDS))
  }

}
