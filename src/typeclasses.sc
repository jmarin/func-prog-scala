object Typeclass:

  // Part 1 - Type class definition

  case class Person(name: String, age: Int)

  trait JSONSerializer[T]:
    def toJson(value: T): String

  // Part 2 - Define type class instances

  given stringSerializer: JSONSerializer[String] with
    override def toJson(value: String): String = "\"" + value + "\""

  given intSerializer: JSONSerializer[Int] with
    override def toJson(value: Int): String = value.toString()

  given personSerializer: JSONSerializer[Person] with
    override def toJson(person: Person): String = s"""
      {"name": ${person.name}, "age": ${person.age}}
    """.stripMargin.trim()

  // Part 3 - User facing API

  def convert2Json[T](value: T)(using serializer: JSONSerializer[T]): String =
    serializer.toJson(value)

  def convertList2Json[T](list: List[T])(using
      serializer: JSONSerializer[T]
  ): String =
    list.map(value => serializer.toJson(value)).mkString("[", ",", "]")

  // Part 4 - Optional. You can add extension methods for the types you support

  extension [T](value: T)
    def toJson(using serializer: JSONSerializer[T]): String =
      serializer.toJson(value)

// This makes the typeclass pattern very expressive
import Typeclass.*

println(convertList2Json(List(Person("Alice", 23), Person("Bob", 46))))
val bob = Person("Bob", 46)
println(bob.toJson)
