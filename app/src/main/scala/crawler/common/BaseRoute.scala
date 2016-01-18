package crawler.common

import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.LazyLogging
import crawler.SystemUtils

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-01-18.
  */
trait BaseRoute extends Directives with JsonSupport with LazyLogging {
  implicit def system = SystemUtils.system

  implicit def materializer = SystemUtils.materializer

  implicit def dispatcher = system.dispatcher
}
