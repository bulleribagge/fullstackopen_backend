package fto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol}

case class CreatePerson(name: String, number: String)

case class Person(_id: String, name: String, number: String)

object PersonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val createPersonFormat = jsonFormat2(CreatePerson)
  implicit val personFormat = jsonFormat3(Person)
}






