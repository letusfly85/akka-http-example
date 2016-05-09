package org

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import spray.json.DefaultJsonProtocol._
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContextExecutor}

final case class MyKey(userId: String, key: String)
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[MyKey] = jsonFormat2(MyKey)
}

trait Service extends JsonSupport  {

  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  val hogeActor: ActorRef

  def config: Config
  def logger: LoggingAdapter

  val routes =
    pathPrefix("api" / "v1" / "status") {
      (get | post) {
        logger.info("test")
        complete("alive")
      }

    } ~ pathPrefix("api" / "v1" /  "sample") {
      post {
        entity(as[MyKey]) { myKey: MyKey =>
          implicit val timeout = Timeout(5 seconds)
          val future = hogeActor ? myKey
          val result = Await.result(future, timeout.duration).asInstanceOf[HttpResponse]

          result.entity match {
            case HttpEntity.Strict(_, data) =>
              println(data.decodeString("UTF-8"))

              complete("""{"status":"OK"}""")

            case _ =>
              complete("NG")
          }
        }
      }
    }
}

object Main  extends App with Service {
  override implicit val system: ActorSystem  = ActorSystem("akka-http-sample")
  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: Materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val hogeActor: ActorRef  = system.actorOf(Props[HogeActor])

  Http().bindAndHandle(routes, "0.0.0.0", 8080)
}