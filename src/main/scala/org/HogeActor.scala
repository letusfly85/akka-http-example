package org

import akka.actor.{ActorSystem, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import HttpMethods._
import akka.stream.ActorMaterializer
import akka.util.{Timeout, ByteString}

import scala.concurrent.Await
import scala.concurrent.duration._

class HogeActor extends Actor {
  import akka.pattern.pipe

  implicit val system = ActorSystem("actors")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()(system)

  def receive = {
    case MyKey(userId, key) =>
      val uri = "http://localhost:4000/washlets/list"
      val request = HttpRequest(POST, uri)
      implicit val timeout = Timeout(4 seconds)

      //if using below, response will send to below HttpResponse case pattern
      //val future = Http().singleRequest(request).pipeTo(self)

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
