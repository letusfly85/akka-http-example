package org

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{RootJsonFormat, DefaultJsonProtocol}

final case class NormalEmployee(userId: String)
final case class KeyPlayer(userId: String, key: String)
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val keyPlayerFormat: RootJsonFormat[KeyPlayer] = jsonFormat2(KeyPlayer)
  implicit val normalEmployeeFormat: RootJsonFormat[NormalEmployee] = jsonFormat1(NormalEmployee)
}

