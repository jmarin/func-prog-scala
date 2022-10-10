## Contextual Abstractions, typeclasses and monads

  * [Contextuals abstractions in Scala 3](#contextual-abstractions-in-scala-3)
  * [Extension Methods](#extension-methods)
  * [Typeclass Pattern](#typeclass-pattern)
  * [Sequential computations](#sequential-computations)
  * [Monoid](#monoid)
  * [Functor](#functor)
  * [Monad](#monad)

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

println(now.toJSON) // {"now": "2022-10-10T13:16:31.747331738Z"}

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

println(convertList2Json(List(Person("Alice", 23), Person("Bob", 46)))) // [{"name": Alice, "age": 23},{"name": Bob, "age": 46}]
val bob = Person("Bob", 46)
println(bob.toJson) // {"name": Bob, "age": 46}

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

### Purely Algebraic Data Structures

By now, hopefully you have gotten a taste of some data structures that share a certain _shape_, that is, they behave the same way and can use the same type of functions, regardless of the type they operate on or contain. We are going to explore a few pure _algebraic functional structures_, that is, structures that we entirely defined by their _algebra_. This _algebra_ is the set of operations they support, which in turn defines the `laws` they have to comply with. Other than sharing the same laws, instances of these structures have little to do with each other. But thanks to this algebraic behavior it allows us to write polymorphic code that is very generic and reusable. 

The names of some of these algebraic structures come from a branch of Mathematics called [Category Theory](https://en.wikipedia.org/wiki/Category_theory). It is not necessary to know this in order to be a proficient FP developer


#### Monoid

`Monoids` are everywhere, whether you realize it or not. They are one of the simplest algebras that allow you to combine two types in order to produce another value of the same type. 

A `Monoid` is described by:

* Some type A
* An associative binary operation
* An identity element

For example, the sum operation for real numbers is a `Monoid`. It is associative and produces another number. It's "identity" element is the number zero. The same laws applied to multiplication produce an identity element of 1. 

The general shape of a `Monoid` can be described as follows:

```scala
trait Monoid[A]:
  def combine(left: A, right: A): A
  def zero: A
```

With this, we can construct a few instances of Monoids for some base types:

```scala
// String

val stringMonoid = new Monoid[String]:
  def combine(left: String, right: String): String = left + right
  def zero: String = ""

println(stringMonoid.combine("a", "b"))
println(stringMonoid.combine("a", ""))
```


**Example: Combining Maps**

```scala
object Monoids:

  trait Monoid[A]:
    def combine(left: A, right: A): A
    def zero: A

  given stringMonoid: Monoid[String] = new Monoid[String]:
    def combine(left: String, right: String): String = left + right
    def zero: String = ""

  given intAddition: Monoid[Int] = new Monoid[Int]:
    def zero: Int = 0
    def combine(a: Int, b: Int) = a + b

  given mapMonoid[K, V](using Monoid[V]): Monoid[Map[K, V]] =
    new Monoid[Map[K, V]]:
      override def zero: Map[K, V] = Map.empty[K, V]
      override def combine(left: Map[K, V], right: Map[K, V]): Map[K, V] =
        val monoid = summon[Monoid[V]]
        (left.keySet ++ right.keySet).foldLeft(zero) { (acc, k) =>
          acc.updated(
            k,
            monoid.combine(
              left.getOrElse(k, monoid.zero),
              right.getOrElse(k, monoid.zero)
            )
          )
        }

import Monoids.Monoid
import Monoids.given

val m1 = Map("a" -> 1, "b" -> 2, "c" -> 3)
val m2 = Map("d" -> 4, "e" -> 5)

given monoid: Monoid[Map[String, Int]] = mapMonoid[String, Int]

val m3 = monoid.combine(m1, m2)
println(m3) // HashMap(e -> 5, a -> 1, b -> 2, c -> 3, d -> 4)
```

---

#### Functor

A `Functor` is an algebraic structure that generalizes the `map` function. In Category Theory terms, it describes a mapping between categories. This usually referes to transformation of data or functions, and it is one of the cornerstones of any functional programming language. Most mainstream languages have adopted the capability to `map` certain data structures to derive new data strutures by applying a function (usually a closure).

As we have already seen, many of the "container" data structures such as `Option`, `List`, `Future`, etc. have functor properties. Does this mean that any structure that implements `map` is a `Functor`?. No. In order to be a proper `Functor`, the structure needs to comply with the `Functor Laws`:

* **Identity**: applying `map` to the identity function should return the same container without any changes 

```scala
fa.map(x => x) = fa
```

* **Composition**: applying `map` on a function `f` and then applying `map` on the function `g` of the original result should result in the same as applying `map` to the function composition of `f` and `g`

```scala
fa.map(f).map(g) = fa.map(f.andThen(g))
```

A generic signature for a functor can be expressed as follows (assume C to be a "container"):

```scala
trait Functor[C[_]]:
  def map[A, B](c: C[A])(f: A => B): C[B]
```

With "C" being a container, or a higher-kinded type as discussed before. 

**A Functor allows us to generalize an API on mappable structures in a uniform way without needing to repeat ourselves**


Example 1: generalize the definition of Functor for several "containers"

```scala
trait Functor[C[_]]:
  def map[A, B](c: C[A])(f: A => B): C[B]

given listFunctor: Functor[List] = new Functor[List]:
  override def map[A, B](container: List[A])(f: A => B): List[B] =
    container.map(f)

given optionFunctor: Functor[Option] = new Functor[Option]:
  override def map[A, B](container: Option[A])(f: A => B): Option[B] =
    container.map(f)

// General API for `map`

def multiplyBy10[C[_]](container: C[Int])(using functor: Functor[C]): C[Int] =
  functor.map(container)(_ * 10)

// This works now for any instance of givens Functor

val list = List(1, 2, 3)
println(Functors.multiplyBy10(list)) // List(10, 20, 30) 
```

Example 2: implementing a Functor for a custom data type

```scala
trait Functor[C[_]]:
  def map[A, B](c: C[A])(f: A => B): C[B]

given listFunctor: Functor[List] = new Functor[List]:
  override def map[A, B](container: List[A])(f: A => B): List[B] =
    container.map(f)

given optionFunctor: Functor[Option] = new Functor[Option]:
  override def map[A, B](container: Option[A])(f: A => B): Option[B] =
    container.map(f)

// General API for `map`

def multiplyBy10[C[_]](container: C[Int])(using functor: Functor[C]): C[Int] =
  functor.map(container)(_ * 10)

// This works now for any instance of givens Functor....

import Functors.*

val list = List(1, 2, 3)
println(Functors.multiplyBy10(list)) // List(10, 20, 30)

// Binary Tree
enum Tree[+A]:
  case Leaf[+A](value: A) extends Tree[A]
  case Branch[+A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]

object Tree:
  def leaf[A](value: A): Tree[A] = Leaf(value)
  def branch[A](value: A, left: Tree[A], right: Tree[A]): Tree[A] =
    Branch(value, left, right)

import Tree.*

given treeFunctor: Functor[Tree] = new Functor[Tree]:
  override def map[A, B](container: Tree[A])(f: A => B): Tree[B] =
    container match
      case Leaf(value) => Leaf(f(value))
      case Branch(value, left, right) =>
        Branch(f(value), map(left)(f), map(right)(f))


// ...including instances of custom data structures

val tree =
  Tree.branch(1, Tree.branch(2, Tree.leaf(3), Tree.leaf(4)), Tree.leaf(5))

println(tree)
println(multiplyBy10(tree))

// By leveraging extension methods we can also add a map method to a Functor

extension [C[_], A, B](container: C[A])(using functor: Functor[C])
  def map(f: A => B): C[B] = functor.map(container)(f)

def treeMultiplyBy10 =
  tree.map(_ * 10) // gets transformed into treeFunctor.map(tree)(_ * 10)

// We've just added the same syntax that List, Option, etc have. We can map Tree[A] the same we do Option[A] or List[A]

println(treeMultiplyBy10)
```

---

#### Monad

The concept of a `Monad` is one of those in Functional Programming that sounds esoteric, complicated, and puts many people off when first hearing about it. 

A `Monad` is a mechanism to describe sequential computations with some additional features (effects). These effects can be the possibility of a value being null, a computation that can fail, that is asynchronous, etc. They are expressed with a Type constructor that is itself a `Monad` (i.e. `Option[A]`)

A Monad must have two fundamental methods:

* The ability to wrap a value into a `Monad`: `pure`
* The ability to transform values from one type of another, wrapped inside of a container

Example 1: Option:

```scala
case class User(firstName: String, lastName: String):
  require(firstName != null && lastName != null)

// Let's get the user, the stupid way
def getUser(firstName: String, lastName: String): User = 
  if (firstName != null) then
    if (lastName != null) then
      User(firstName, lastName)
    else
      null
  else
    null
```

We first extract the first name, if that is not null then we extract the last name, and if that is also not null, then we construct our User instance. This pattern is everywhere. The imperative code such as the one above can be found everywhere too, and it is very bad

A better Option (pun intended)

```scala
def optionUser(firstName: String, lastName: String): Option[User] =
  Option(firstName).flatMap( fName => 
    Option(lastName).flatMap( lName =>
      Option(User(fName, fName))  
    )  
  )

// Even better, with for-comprehensions

def optionUser2(firstName: String, lastName: String): Option[User] =
  for 
    fName <- Option(firstName)
    lName <- Option(lastName)
  yield User(fName, lName)
```

We don't have to program defensively, executing the "happy path". But if there is no value, we get `None`

A very similar structure would be present if we used `Future`, `List`, etc. All of them have the following: 

* A "constructor": a way to take a value and put it inside the container. Usually called `unit` or `pure`
* A transformation function that allows for sequential computations on the wrapped types. Usually called `flatrMap`

A `Monad`, in addition to this, has som properties or _Laws_. This is the part where it gets a bit complicated. I am going to include some syntax to describe what they do, but from a practical point of view don't worry too much about this for now. Just know that adding `pure` and `flatMap` is not enough to create a real `Monad`

* Left Identity --> `Moand.pure(x).flatMap(f) = f(x)`
* Right Identity --> `x.flatMap(y => Moand.pure(y)) = x`
* Associativity --> `m.flatMap(f).flatMap(g) = m.flatMap(x => f(x).flatMap(g))`

The last property defines that a `Monad` is a sequential computation in the general term . 

The Monad structure and its laws defines a Functional Programming pattern that is generic and reusable for resolving a generic problem, usually involving the resolution of sequential computations on different types


The important thing to remember about a Monad is that:

* it has a `map` method (this is because it is derived from a `Functor`)
* it has a `flatMap` method
* it has a `lift` or `pure` method to "lift" another type into the `Monad`


