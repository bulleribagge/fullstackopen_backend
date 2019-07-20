package fto


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Person(id: Option[Int], name: String, number: String)

object PersonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val noteFormat = jsonFormat3(Person)
}
