package fto

import scala.util.Random

class PersonService {

  private val ran = new Random()

  private var persons: Seq[Person] = Seq[Person](
    Person(Some(1), "Arto Hellas", "040-123456"),
    Person(Some(2), "Ada Lovelace", "39-44-5323523"),
    Person(Some(3), "Dan Abramov", "12-43-234345")
  )

  def getAllPersons: Seq[Person] = {
    persons
  }

  def getPerson(id: Int): Option[Person] = {
    persons.find(p => p.id.get == id)
  }

  def addPerson(person: Person): Person = {
    if(persons.count(p => p.name == person.name) == 0) {
      val newPerson = person.copy(id = Some(ran.between(0, 10000000)))
      persons = persons.concat(Seq(newPerson))
      newPerson
    }else{
      throw new IllegalArgumentException("name must be unique")
    }
  }

  def removePerson(id: Int): Unit = {
    persons = persons.filter(n => n.id.get != id)
  }
}
