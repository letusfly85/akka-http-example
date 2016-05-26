package org

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import HttpMethods._
import MediaTypes._

import scala.concurrent.{Await, ExecutionContextExecutor}


trait Service extends JsonSupport  {
  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  val delegateActor: ActorRef
  val startTimeMillis = System.currentTimeMillis()

  def config: Config
  def logger: LoggingAdapter

  val routes =
    pathPrefix("api" / "v1" / "status") {
      (get | post) {
        logger.info("test")
        complete("alive")
      }

    } ~ pathPrefix("api" / "v1" /  "reflect") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          val uri = "http://localhost:8080/api/v1/sample"
          val data = s"""{ "userId": "${myKey.userId}", "key": "${myKey.key}" }"""
          val request: HttpRequest = new HttpRequest(POST, uri,
            entity = HttpEntity(`application/json`, data
            )
          )
          Http().singleRequest(request)

          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis
          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}\n")
        }
      }

    } ~ pathPrefix("api" / "v1" /  "one") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          val startTimeMillis = System.currentTimeMillis()
          delegateActor ! KeyPlayer(myKey.userId, myKey.key)
          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis

          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}n")
        }
      }

    } ~ pathPrefix("api" / "v1" /  "sample") {
      post {
        entity(as[KeyPlayer]) { myKey: KeyPlayer =>
          delegateActor ! NormalEmployee(myKey.userId)
          val endTimeMills = System.currentTimeMillis()
          val estimate = endTimeMills - startTimeMillis

          complete(s"${myKey.userId}-${myKey.key}-${estimate.toString}\n")

          /*
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
          */
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

  override val delegateActor: ActorRef  = system.actorOf(Props[DelegateActor], "my-dispatcher")

  Http().bindAndHandle(routes, "0.0.0.0", 8080)
}