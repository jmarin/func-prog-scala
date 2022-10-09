## Contextual Abstractions, typeclasses and monads

  * Contextuals abstractions in Scala 3
  * Extension Methods
  * Typeclass Pattern
  * Sequential computations
  * Monoid
  * Functor
  * Applicative
  * Monad

---

### Contextual abstractions in Scala 3

One of the signature characteristics of `Scala` was the use of `implicits`. Both loved and hated, they required a bit of care when used with `Scala` 2. 
Implicits had a few use cases:

- Implementing type classes
- Establishing context
- Dependency injection
- Expressing capabilities
- Deriving new types (implicit conversions)

Other languages were inspired by this, for example `Rust` with traits and `Swift` with its _protocol extensions_. Other languages like `C#` and `F#` have proposals to incorporate similar concepts. 

All of these follow the same idea: **given a type, the compiler creates a "canonical" instance of that type**

The problems with implicits in `Scala` 2 are that this keyword is used to express different intents. Are we trying to perform an implicit conversion? (by the way, you probably don't want to do this in general).
Or pass in implicit parameters for dependency injection?. Are we building a Typeclass?. 

In `Scala` 3 the `implicits` space got an overhaul, for the typical use cases:

* Contextual information (configuration, settings, etc) can be provided by using the `using` keyword

Example:

```scala
def uploadToCloud(file: File)(using C: CloudConfig): Unit
```

By declaring a parameter with `using` we are telling the compiler to perform `term inference`, that is, to find an instance in scope of the proper type, and inject it **at compile time**.

Becasue this instance is "implicit", we don't even need to assign a variable anymore, making the code more succint

```scala
def uploadToCloud(file: File)(using CloudConfig): Unit
```

* Extend types that we don't have the source code for. This can be accomplished through _extension methods_, explained in the next section. 

* Provide Typeclass instances. See section below on how to construct them, as well as practical applications. 

* View one type as another. This replaces `Scala` 2 _implicit conversions_ through  the use of a Typeclass called `Conversion`. We will not cover it in this course, but feel free to read some [documentation](https://docs.scala-lang.org/scala3/book/ca-implicit-conversions.html) about it. Remember that this technique can introduce "surprises" in the behavior of a system, and is to be used carefully. 

* Higher-order contextual abstractions. This is now a first class citizen and a new feature of the `Scala` 3 language, through the use of `context functions`. We will not be covering them in this course, as this topic is rather advanced and only comes up when designing libraries or using advanced libraries.

`Scala` 3 compiler is much improved when it comes to giving feedback when implicit parameters can't be resolved. 

---


### Extension Methods

Extension methods allow defining new methods to third party types. Even if you don't have the original source code, you can extend the behavior of those types through this mecahnism. This is one of many features that required the use of the `implicit` keyword in `Scala` 2.
These methods feel completely integrated and can be used as any other method already present in the type. 

**Example:** 

Suppose we want to print the current date in a particular `JSON` format. On the JVM, we can use the `java.time.Instant` to get the current time. It has a method, `toString()`, that represents the current time in `ISO-8601` format. But we want a `JSON` representation. We can "extend" this class as follows

```scala
import java.time.Instant

object TimeExtensions:
  extension (t: Instant)
    def toJSON: String =   
      s"""{"now": "${t.toString()}"}"""


import TimeExtensions.*

val now = Instant.now()

println(now.toJSON)

```

---

### Typeclass Pattern

Typeclasses refer to polymorphic type systems. A Typeclass defines a set of methods that is shared across multiple types. For a type to belong to a typeclass, it must implement the methods of that typeclass. These implementations are _ad-hoc_, which means that the methods can have different implementations for different types. This type of polymorphism is usually called _ad-hoc polymorphism_ and it is a very powerful technique to model abstract behavior for your software, that can be extended for the particular types that you need to work with.

**Example**

```scala
// Part 1 - Type class definition: this is usually done with a generic trait

  case class Person(name: String, age: Int)

  trait JSONSerializer[T]:
    def toJson(value: T): String

  // Part 2 - Define type class instances: implementations of the types that are part of the Typeclass

  given stringSerializer: JSONSerializer[String] with
    override def toJson(value: String): String = "\"" + value + "\""

  given intSerializer: JSONSerializer[Int] with
    override def toJson(value: Int): String = value.toString()

  given personSerializer: JSONSerializer[Person] with
    override def toJson(person: Person): String = s"""
      {"name": ${person.name}, "age": ${person.age}}
    """.stripMargin.trim()

  // Part 3 - User facing API: here we leverage the contextual abstractions in Scala 3. For this method, if there is an implicit instance of JSONSerializer for the type T, the compiler will inject it in scope and use it

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

```


---


### Sequential computations: `map`, `flatMap` and `for-comprehensions`

#### Parallel Collections

Scala has an optional [library](https://github.com/scala/scala-parallel-collections) that allows you to execute higher order functions (such as map) on collections in _parallel_.

For example, if we want to sum the first 1 million integer numbers, we can do it this way

```scala
(1 to 1000000).toArray.map(_ + 1).reduce(_ + _)
```
The same computation can be done by leveraging parallel collections, by transforming the numbers to a parallel array. The computations will be done in parallel

```scala
(1 to 1000000).toArray.par.map(_ + 1).reduce(_ + _)
```

Parallel collections are not recommended for small collections. For large collections they have the potential to speed up your computation by using multiple CPU cores at the same time. 

It is important to note that this mechanism relies on the JVM and its threading model. While some other dynamic languages (Python, Javascript) also have parallel libraries / frameworks what we are talking about here is leveraging **native** capabilities of the runtime, resulting in a more integrated programming experience and better performance. 

#### Sequential Computations

Sequential computations are the backbone of enterprise software development. Do this, and then do that, and if this other condition applies, then do the next thing.....this type of reasoning permeates software development in most domains. Functional Programming helps model these complex domains, and `Scala` provides some syntactic sugar to ease the interpretation of code written. 

Consider the following code

```scala
case class User(id: String, name: String, roleId: String)
case class Role(id: String, isAdmin: Boolean)
case class Authorization(
    roleId: String,
    capabilities: List[String] = List.empty
)

object UserRepository:
  private val users =
    List(User("1", "John", "1"), User("2", "Alice", "2"), User("3", "Bob", "2"))
  def retrieve(id: String): Option[User] = users.filter(_.id == id).headOption

object RoleRepository:
  private val roles = List(Role("1", true), Role("2", false))
  def retrieve(id: String): Option[Role] = roles.filter(_.id == id).headOption

def getUser(userId: String): Option[User] = UserRepository.retrieve(userId)
def getRole(roleId: String): Option[Role] = RoleRepository.retrieve(roleId)
def getAuthorization(role: Role): Option[Authorization] =
  if role.isAdmin then Some(Authorization(role.id, List("all")))
  else None

def authorize(userId: String): Option[Authorization] =
  getUser(userId)
    .flatMap(u => getRole(u.roleId))
    .flatMap(r => getAuthorization(r))

def authorize2(userId: String): Option[Authorization] =
  for
    user <- getUser(userId)
    role <- getRole(user.roleId)
    auth <- getAuthorization(role)
  yield auth
```

`for-comprehensions` are a convenient way to express sequential computations. They are syntactic sugar for the `map`, `flatMap` and `filter` operations. Writing sequential computations with `for-comprehensions` takes a bit of getting used to, but they offer a concise and standard way to express them that is easier to read and thus maintain. 

---

### Monoid

---

### Functor

---

### Applicative

---

### Monad


