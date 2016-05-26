package org

import java.util.UUID

import akka.actor.{Props, ActorRef, ActorSystem, Actor}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import HttpMethods._
import akka.stream.ActorMaterializer
import akka.util.{Timeout, ByteString}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

case class Employee(userId: String)
class DBWriteActor extends Actor {
  val logger: LoggingAdapter = Logging(context.system, getClass)

  def receive = {
    case Employee(userId) =>
      // do something for database
      val sleepTime = Random.nextInt(5) * 1000L
      Thread.sleep(sleepTime)

      self ! userId

    case userId: String =>
      logger.info(s"${userId} is stopping... ${self.path}, ${self.toString()}")
      context.stop(self)
  }

  override def postStop: Unit = {
    logger.info(s"${self.path.name} stopped")
  }

}

class DelegateActor extends Actor {
  import akka.pattern.pipe

  implicit val system = ActorSystem("actors")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()(system)

  def receive = {
    case Employee(userId) =>
      val actorName = s"${userId}-${UUID.randomUUID}"
      val dbWriteActor: ActorRef = context.actorOf(Props[DBWriteActor], actorName)
      dbWriteActor ! Employee(userId)

    case KeyPlayer(userId, key) =>
      val uri = "http://localhost:4000/washlets/list"
      val request = HttpRequest(POST, uri)
      implicit val timeout = Timeout(4 seconds)

      val future = Http().singleRequest(request)//.pipeTo(self)
      val result = Await.result(future, timeout.duration)

      sender() ! result

    case HttpResponse(StatusCodes.OK, headers, entity, _) => {
      entity match {
        case HttpEntity.Strict(_, data) =>
          println(data.decodeString("UTF-8"))

        case _ =>
          println("error!")
      }
    }

    case _ =>
      println("error!")
      sender() ! "error"
  }

}
