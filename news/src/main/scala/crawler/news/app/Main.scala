package crawler.news.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-03.
 */
object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


}
