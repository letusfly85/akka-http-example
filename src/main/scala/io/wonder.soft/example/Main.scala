package io.wonder.soft.example

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.ContentTypes._
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.ByteString
import com.typesafe.config.{ Config, ConfigFactory }
import io.wonder.soft.example.actor.DelegateActor

import scala.concurrent.ExecutionContextExecutor

trait Service extends JsonSupport {
  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  val delegateActor: ActorRef
  val startTimeMillis = System.currentTimeMillis()

  def config: Config
  def logger: LoggingAdapter

  val routes =
    path("api" / "v1" / "status") {
      (get | post) {
        logger.info("test")
        complete("alive")
      }

    } ~ path("api" / "v1" / "reflect") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          val uri = "http://localhost:8080/api/v1/sample"
          val data = ByteString(s"""{ "userId": "${myKey.userId}", "key": "${myKey.key}" }""")
          val request = HttpRequest(POST, Uri(uri))
            .withEntity(`application/json`, data)

          Http().singleRequest(request)

          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis
          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}\n")
        }
      }

    } ~ path("api" / "v1" / "one") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          val startTimeMillis = System.currentTimeMillis()
          delegateActor ! KeyPlayer(myKey.userId, myKey.key)
          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis

          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}n")
        }
      }

    } ~ path("api" / "v1" / "sample") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          delegateActor ! NormalEmployee(myKey.userId)
          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis

          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}\n")
        }
      }
    }
}

object Main extends App with Service {
  override implicit val system: ActorSystem = ActorSystem("akka-http-sample")
  override implicit val executor: ExecutionContextExecutor = system.dispatcher
  override implicit val materializer: Materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val delegateActor: ActorRef =
    system.actorOf(Props[DelegateActor], "my-dispatcher")

  Http().bindAndHandle(routes, "0.0.0.0", 8080)
}
