package crawler.testsuite

import java.util.concurrent.TimeUnit

import crawler.SystemUtils
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

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

  implicit val system = SystemUtils.system
  implicit val materializer = SystemUtils.materializer
  implicit val defaultPatience = PatienceConfig(Span(30, Seconds))

  override protected def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination(Duration(10, TimeUnit.SECONDS))
  }

}
