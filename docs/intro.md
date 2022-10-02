---
title: Introduction to Functional Programming
author: Juan Marin Otero
extensions:
  - image_ueberzug
---

## Introduction to Functional Programming
   
   * Why Functional Programming
   * Pure functions
   * Side Effects
   * Referential Transparency
   * Higher-order Functions
   * Functional Data Structures
   * Compositionality
   * Polymorphism through Higher-Kinded Types
   * Handling errors without Exceptions (Option, Try, Either data types)
   * There is a problem in the Future

---

### Why Functional Programming

Functional Programming is writing programs with _functions_. This style of programming is _declarative_ vs _imperative_.
We tell the computer what to do, now how. 
- Apply functions to evaluate expressions
- Little emphasis on explicit sequencing (control flow)
- Higher order functions, composition, recursion, etc.

#### Principles of Functional Programming

- Immutability: prefer `val` over `var`. A `var` in the code should be questioned **always**. Is it necessary? Can I express this with an immutable `val`?

```scala
final case class User(firstName: String, lastName: String)
val u = User("John", "Doe")
```
This case class instance represents an _immutable_ data structure. It cannot be changed or modified. That means there are no race conditions, no deadlocks. Very good for parallel code!

- Avoid _shared mutable state_: no code should mutate state that is shared with other parts of the program. 

- Functions as first class citizens: functions are the building blocks of a program. They can be passed as arguments and returned as the final statement of another function

- Functions are _pure_: no side effects. The use of pure functions leads to a nice property called **Referential Transparency**.  We will dive deeper into these concepts in a bit.


#### Advantages
* Sound, correct proof that a program or even a whole system will work
* Easier to reason about and maintain
* Easier to parallelize
* Great fit for data heavy, complex domain modeling

#### Disadvantages

* High cognitive load --> steep learning curve
* Discipline is required to reap the benefits
* In some cases, memory consumption and performance is not on par with imperative options

**Example: calculate fibonacci sequence**

This first version is not stack safe. It is only valid for small numbers

```scala
def fib(n: Long): Long = n match
  case 0 | 1 => n
  case _ => fib(n -1) + fib(n-2)
```

This second version uses **tail recursion**: the last statement of a function is a recursive call to iself. The Scala compiler will create a loop that is safe and performs well

```scala
def fib(n: Int): Int = 
  def fib0(n: Int, a: Int, b: Int): Int = n match
    case 0 => a
    case _ => fib0(n - 1, b, a + b)
  fib0(n, 0, 1)
```

Bonus version: getting crazy with lazy evaluation using Scala Streams and Memoization --> maximum performance!

```scala
import scala.math.BigInt
lazy val fib: Stream[BigInt] = BigInt(0)#::BigInt(1)#::fib.zip(fib.tail).map(p => p._1 + p._2)

```

Calling `fib take 20 foreach println` will print the first 20 fibonacci numbers

I don't expect you to fully understand this last example. But hopefully by the end of this course this will make more sense

---

### Pure Functions

A pure function is a function that, given the same inputs (arguments) produces the same results every time.

A pure function does not produce side effects.

A pure function is _composable_


Example of pure function

```scala
def addTen(a: Int): Int = a + 10 
```


Example of impure function

```scala
def addTen2(a: Int): Int = 
  println(a)
  a + 10 
```

This function is impure because it prints to the console, which is a _side effect_

---

### Side Effects

A side effect is anything in a function that doesn't return a result, for example:

- Modifying a variable
- Modifying a data structure in place
- Setting a field on an object
- Throwing an exception or halting with an error
- Printing to the console or reading user input
- Reading from or writint to a file
- Drawing to the screen
- Talking to a database or external service

Why mixing domain logic and side effects is bad:

- Separation of concerns. Domain logic and side effects need to work together, but are not responsible for the same thing. Coupling these two makes maintenance and evolution harder
- Difficult in testing. Unit tests become difficult if they require a database to be running. 
- If I am modeling my domain mostly with functions, but they are not pure.....how easy is it to reason about my domain model?. It's harder with side effects
- Functions with side effects are not composable. We end up with a lot of boilerplate and glue code that has nothing to do with our domain. 

**How in the world are we going to build anything useful with FP if all these operations make our functions impure?**

---


### Referential Transparency

An expression is referentially transparent if it can be replaced by its value (and viceversa) without changing the program's behavior. This requires that the expression be _pure_

Functional Programming (FP) only allows the use of referentially transparent functions

Functions that have side effects are impure from an FP point of view and can no longer be considered referentially transparent expressions
  - We can't apply the substitution model and expect it to work the same way every time
  - We can't replace them with the value they produce and expect the program not to change
  - Harder to reason about (local reasoning vs global)

---

### Higher-order Functions

Functions are _values_. As such, they can be passed around as parameters to other functions. And they can also be returned as the output of a computation inside a function, storead in a data structure or assigned to a variable.

A _Higher-order function_ is a function that accepts other functions as parameters and / or returns a function as a result. These types of functions are very useful in FP. 

Example 1 --> the `map` function, allows transformation of values inside a "container"

```scala
val values = List(1,2,3,4,5)
val double = (x: Int) => x * 2
val doubleValues = values.map(double)
```

Can we derive the type signature (simplified) of `map`?

```scala
trait MyMap: 
  def map[B](f: (A) => B): List[B]
```

Example 2 --> the `flatMap` function, allows transformation of containers of values inside another container, with a flattening of the result

```scala
val values = List(Some(1), Some(2), None, Some(4), Some(5))
val double = (x: Option[Int]) => x.map(_ * 2)
val doubleMapValues = values.map(double)
val doubleFlatMapValues = values.flatMap(double)

```

Can we derive the type signature (simplified) of `flatMap`

```scala
trait MyFlatMap:
  def flatMap[B](f: (A) => List[B]): List[B]
```


---

### Functional Data Structures

Data structures that are operated on by pure functions. As such, Functional Data Structures are _immutable_.
Functional data structures share data, which means that when deriving a new instance we don't have to copy all the data.

For example, when adding a new element to a list, we get a new list. But the way it's implemented, we don't need to copy the whole original list

These data structures, many of which are present in the Scala standard library, are persistent. This is an optimization so that we don't consume
more memory than is necessary when operating on these data structures. The `JVM` is **really good** at clearing up what is no longer used through 
garbage collection. 

Example: immutability in Scala

- Always use `final case class` when defining your data
- Always use `val` when assigning variables
- Never mutate state (i.e. variable changing values inside a loop)
- Use `scala.collection.immutable` versions of collections by default

Exceptions to the above are exceptional and must have a good reason. 

Immutability _could_ have performance considerations but it is a safety concern. `Scala` is extremely optimized to leverage persistent data structures. Use them!

---

### Compositionality

**Principle of Compositionality (Wikipedia)**: _the principle that the meaning of a complex expression is determined by the meanings of its constituent expressions and the rules used to combine them_

#### Function Composition

We can "stack" functions and combine them, to perform more complex transformations of data. If we use pure functions, all the software we write can be made up of smaller pieces based on functions.

```scala
def f(x:A): B

def g(y: B) => C
```

`g(f(a))` is of type `A => B => C`


Many times, function composition rules follow the same semantics as mathematical functions (associative, commutative)


#### Algebraic Data Types

Algebraic Data Types (ADT) are a data type that is composed of other types. The most common ADTs are `Sum Types` and `Product Types`. 

**Product types** are typically represented by `case classes` in `Scala`. The set of all possible values of a product type is the Cartesian product

```scala
case class IsRegistered(registered: Boolean, validated: Boolean)
```

The amount of possible values for this case class is 4 (2 * 2). Pattern matching can be used on a product type to extract different combinations:

```scala
val r: IsRegistered = ???
r match
  case (true, true) => ??? 
  case (true, false) => ???
  case (false, true) => ???
  case (false, false) => ???
```

**Sum Types** are implemented in `Scala` by way of inheritance. They represent the logical `OR` disjunction, this type OR this other type

In Scala 2, they are represented with sealed traits and case objects or case classes

```scala
sealed trait Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color
```

In Scala 3, they are represented with the new enum data structure

```scala
enum Color:
  case Red, Green, Blue
```

The `Color` Sum Type represents three possible values

Product Types and Sum types combined can form an ADT


```scala
sealed trait Option[+A]
case object extends Option[Nothing]
case class Some[A](a: A) extends Option[A]
```

---


### Polymorphism through Higher-Kinded Types

What the `F[_]`?

A higher-kinded type is a type that abstracts over some type that, in turn, abstracts over another type. Another way to put it: it's a type with a type constructor

Usually represented like this: `F[_]` with `F` being the higher-kinded type. 

But Why???

Higher-kinded types are used for the purpose of **abstraction**

They are a powerful technique in `Scala` to defined polymorphic behavior, especially when designing the interface of a library / program. 

For example, consider the following API:

```scala
import scala.concurrent.Future
sealed trait Stock
sealed trait StockRank
trait StockApi:
  def rankByPrice(s1: Future[Stock], s2: Future[Stock]): Future[StockRank]
```

Future here represents an asynchronous computation. Maybe we are calling an external service and waiting for it to respond, to get the data we need. Now consider this example: 

```scala
sealed trait Stock
sealed trait StockRank
trait StockApi:
  def rankByPrice(s1: Option[Stock], s2: Option[Stock]): Option[StockRank]
```

Here we are representing a value that might not exist. But doesn't it look very similar to the previous one? Could we express this more generically?

```scala
sealed trait Stock
sealed trait StockRank
trait StockApi[F[_]]:
  def rankByPrice(s1: F[Stock], s2: F[Stock]): F[StockRank]
```

This version has polymorphic behavior, and we can implement different versions for differnet `F[_]` type constructors. `F[_]` is usually a Monad or has monadic behavior (don't worry, we'll get to that)
This API is more generic, more reusable, and has separation of concerns built it (separate definition from implementation). This makes for more modular, flexible and maintainable code. 

---

### Handling Errros without Exceptions
 
* `Option[A]` --> Used to represent the possibility that a value might be absent. Can be pattern matched on `Some(a)` and `None`. 

* `Try[A]` --> Used to "wrap" functions or method calls that can throw exceptions. Used a lot to interact with Java libraries. **Do not use to model "error" in your business domain**

* `Either[E, A]` --> Provides a valid value (A) when the computation is successful, or an error type (E) when it fails. 

The more functional approach to extracting values from this higher-kinded types is to use `flatMap` or `for-comprehension` expressions to execute the "happy path". This, as we will see later, allows for sequential computations with early exit when an error occurs

---


### There is a problem in the Future

So, there is a problem in the `Future` in the `Scala` standard library, because of these two properties:

- Eager evaluation: `Future` starts evaluating its value right after it's defined
- Memoization: once the value is computed, it's shared with other calls, without being recalculated. 

**This means that `Future` is not referentially transparent**

Example: how many times do we see "Hello World" printed out?

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val future = Future(println("Hello World"))
for 
  _ <- future
  _ <- future
  _ <- future
yield ()
```

The code above prints "Hello World" only once because of these properties. It obviously violates referential transparency guarantees and makes it harder to reason about. In order to force the expected behavior, the "correct" 
code is as follows:

```scala
for 
  _ <- Future(println("Hello World"))
  _ <- Future(println("Hello World"))
  _ <- Future(println("Hello World"))
yield ()
```

Another problem with `Future` is that because of its eager execution mode it requires an `ExecutionContext` to be passed around implicitly everywhere a Future needs to run. This pollutes your code with unnecessary contextual 
abstractions (using / givens in Scala 3, implicits in Scala 2)






