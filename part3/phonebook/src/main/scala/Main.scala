package fto

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import PersonFormat._
import akka.dispatch.affinity.RejectionHandler
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route, ValidationRejection}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object Main  {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val exceptionHandler: ExceptionHandler = {
      ExceptionHandler {
        case ex: IllegalArgumentException => {
          println(s"error ${ex.getMessage}")
          complete(HttpResponse(StatusCodes.BadRequest, entity = ex.getMessage))
        }
      }
    }

    val rejectionHandler = {
      RejectionHandler.newBuilder()
        .handle{
          case ValidationRejection(msg, _) =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = msg))
        }.result()
    }

    val personService = new PersonService()

    val route =
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          pathPrefix("api") {
            path("persons") {
              get {
                complete(Future.successful(personService.getAllPersons))
              } ~
                post {
                  entity(as[Person]) { person =>
                    val savedPerson = Future.successful(personService.addPerson(person))
                    complete(savedPerson)
                  }
                }
            } ~
              pathPrefix("persons" / IntNumber) { id =>
                delete {
                  Future.successful(personService.removePerson(id))
                  complete(StatusCodes.NoContent)
                } ~
                  get {
                    val maybePerson = personService.getPerson(id)
                    maybePerson match {
                      case Some(person) => complete(person)
                      case None => complete(StatusCodes.NotFound)
                    }
                  }
              }
          } ~
            path("info") {
              val numPeople = personService.getAllPersons.length
              val responseHtml =
                s"""
                   |<div>Phonebook has info for ${numPeople} people</div>
                   |<div>${ZonedDateTime.now.toLocalDateTime}</div>
                   |""".stripMargin
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, responseHtml))
            }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 3001)

    println("Akka http server running on http://localhost:3001.\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
