package fto

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import PersonFormat._
import akka.http.scaladsl.model.HttpMethods

import scala.util.{Failure, Success}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, ValidationRejection}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import collection.JavaConverters._

object Main {

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
        case ex: PersonDoesNotExistException => {
          println(s"error ${ex.getMessage}")
          complete(HttpResponse(StatusCodes.NotFound, entity = ex.getMessage))
        }
        case ex: Throwable => {
          println(s"Unexpected error ${ex.getMessage}")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = ex.getMessage))
        }
      }
    }

    val rejectionHandler = {
      RejectionHandler.newBuilder()
        .handle {
          case ValidationRejection(msg, _) =>
            complete(HttpResponse(StatusCodes.BadRequest, entity = msg))
        }.result()
    }

    val personService = new PersonService()

    val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedMethods(scala.collection.immutable.Seq(HttpMethods.DELETE, HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.OPTIONS))
    val route =
      handleExceptions(exceptionHandler) {
        handleRejections(rejectionHandler) {
          cors(corsSettings) {
            pathPrefix("api") {
              path("persons") {
                get {
                  complete(personService.getAllPersons)
                } ~
                  post {
                    entity(as[CreatePerson]) { person =>
                      complete(personService.addPerson(person))
                    }
                  }
              } ~
                pathPrefix("persons" / JavaUUID) { id =>
                  delete {
                    personService.removePerson(id)
                    complete(StatusCodes.NoContent)
                  } ~
                    get {
                      complete(personService.getPerson(id))
                    }~
                  put {
                    entity(as[CreatePerson]) { person =>
                      complete(personService.updatePerson(id, person))
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
      }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 3001)

    bindingFuture.onComplete {
      case Success(bound) =>
        println(s"Akka http server running on http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}.\nPress RETURN to stop...")
      case Failure(e) => {
        Console.err.println("Server could not start")
        e.printStackTrace()
        system.terminate()
      }
    }

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
