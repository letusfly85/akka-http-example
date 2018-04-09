package io.wonder.soft.example.actor

import java.util.UUID

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.stream.ActorMaterializer
import io.wonder.soft.example.{ KeyPlayer, NormalEmployee }

import scala.util.Random

class DBWriteActor extends Actor {
  val logger: LoggingAdapter = Logging(context.system, getClass)

  def receive = {
    case NormalEmployee(userId) =>
      // do something for database
      val sleepTime = Random.nextInt(5) * 1000L
      Thread.sleep(sleepTime)

      self ! userId

    case KeyPlayer(userId, key) =>
      val delegateActor: ActorRef = context.actorOf(Props[DelegateActor])
      delegateActor ! userId

    case userId: String =>
      logger.info(s"got last message from ${userId} ${self.path}, ${self.toString()}")
      context.stop(self)
  }

  override def postStop: Unit = {
    //logger.info(s"${self.path.name} stopped")
  }

}

class DelegateActor extends Actor {

  implicit val system = ActorSystem("actors")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()(system)

  val globalWriter: ActorRef = context.actorOf(Props[DBWriteActor])

  def receive = {
    case NormalEmployee(userId) =>
      //each actor initialize make slow frontend replying however, backgroud process is little faster than below.
      val actorName = s"${userId}-${UUID.randomUUID}"
      val dbWriteActor: ActorRef = context.actorOf(Props[DBWriteActor], actorName)
      dbWriteActor ! NormalEmployee(userId)

    case KeyPlayer(userId, key) =>
      //global writer reply qucikly to frontend, however backgroud process is very slow.
      globalWriter ! KeyPlayer(userId, key)

    /*
    case KeyPlayer(userId, key) =>
      val uri = "http://localhost:4000/washlets/list"
      val request = HttpRequest(POST, uri)
      implicit val timeout = Timeout(4 seconds)

      val future = Http().singleRequest(request)//.pipeTo(self)
      val result = Await.result(future, timeout.duration)

      sender() ! result
    */

    case HttpResponse(StatusCodes.OK, headers, entity, _) => {
      entity match {
        case HttpEntity.Strict(_, data) =>
          println(data.decodeString("UTF-8"))

        case _ =>
          println("error!")
      }
    }

    case userId: String =>
      val sleepTime = Random.nextInt(5) * 1000L
      Thread.sleep(sleepTime)

      println(s"got last message from ${userId} ${sender().path.name}")

    case _ =>
      println("error!")
      sender() ! "error"
  }

}
