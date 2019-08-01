package fto

import java.util.UUID

import scala.util.Random
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import Helpers._
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.result.{DeleteResult, UpdateResult}


class PersonService {

  private val dbName = "persons_db"

  private val ran = new Random()
  val mongoClient: MongoClient = MongoClient()

  val codecRegistry = fromRegistries(fromProviders(classOf[Person]), DEFAULT_CODEC_REGISTRY )

  private val db = mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry)

  val personsCollection: MongoCollection[Person] = db.getCollection("persons")

  def getAllPersons: Seq[Person] = {
    personsCollection.find().results()
  }

  def getPerson(id: UUID): Person = {
    val persons: Seq[Person] = personsCollection.find(equal("_id", id.toString)).first().results()
    if(!persons.isEmpty)
      {
        return persons.head
      }
    throw new PersonDoesNotExistException("The requested person does not exist")
  }

  def addPerson(person: CreatePerson): Person = {
    val personCount = personsCollection.countDocuments(equal("name", person.name)).results().head
    println(s"Number of persons with the name ${person.name}: $personCount")
    if(personCount == 0) {
      val newPerson = Person(UUID.randomUUID().toString, person.name, person.number)
      personsCollection.insertOne(newPerson).results()
      newPerson
    }else{
      throw new IllegalArgumentException("name must be unique")
    }
  }

  def removePerson(id: UUID): Unit = {
    val deleteResults: Seq[DeleteResult] = personsCollection.deleteOne(equal("_id", id.toString)).results()
    if(deleteResults.head.getDeletedCount != 1)
      {
        throw new PersonDoesNotExistException("The requested person does not exist")
      }
  }

  def updatePerson(id: UUID, person: CreatePerson): Person = {
    val updateResults: Seq[UpdateResult] = personsCollection.updateOne(equal("_id", id.toString), set("number", person.number)).results()
    if(updateResults.head.getModifiedCount != 1)
      {
        throw new PersonDoesNotExistException("The requested person does not exist")
      }
    personsCollection.find(equal("_id", id.toString)).results().head
  }
}
