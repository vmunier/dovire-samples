import utest._
import utest.ExecutionContext.RunNow

import meta._

@Record
case class Street(street: String, hno: Int)
object Street

@Record
case class Person(name: String, surname: String, www: String) {
  def fullName = s"$name $surname"
}
object Person

object Tests extends TestSuite {
  val tests = TestSuite {
    "tests" - {
      println("in tests")
      assert(true)
    }
  }
}


//object Main extends App {
//  //clear class, pure logic
//  val pureStreet = Street("Abbey Rd", 3)
//  //pureStreet.sqlInsert //does not compile -> OK
//  //Street acting as record
//  val street = Street(123,"Abbey Rd", 4)
//  println(street.sqlInsert)
//  println(Street.classMeta.sqlCreate)
//  //pure Person
//  val purePerson = Person("John","Doe","www.noname.none")
//  println(purePerson.fullName)
//  //Record with Person
//  val person=Person(222,"Heather","Schmidt","www.heatherschmidt.com")
//  println(Person.classMeta.sqlCreate)
//  println(person.sqlInsert)
//}
