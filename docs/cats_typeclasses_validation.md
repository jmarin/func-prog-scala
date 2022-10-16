## Functional Programming with Cats

## Cats Typeclasses
  * [Semigroup](#semigroup)
  * [Monoid](#monoid)
  * [Functor](#functor)
  * [Applicative](#applicative)
  * [Monad](#monad)
  * [ApplicativeError / MonadError](#applicativeerror--monaderror)
  * [Traverse](#traverse)
  * [Monad Transformers](#monad-transformers)

### Semigroup

A type `A` can form a `Semigroup` if it has an associative binary operation.

```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A
```

Associativity means the following is true:

```
combine(x, combine(y, z)) = combine(combine(x,y), z)
```

Usage in Cats:

```scala
import cats.kernel.Semigroup

val map1 = Map("one" -> 1, "two" -> 2)
val map2 = Map("three" -> 3, "four" -> 4)

val mapCombined = Semigroup[Map[String, Int]].combine(map1, map2)
println(mapCombined) //Map(three -> 3, four -> 4, one -> 1, two -> 2)
```

Associativity means that the following are equivalent:

```scala
val leftSum = List(1, 2, 3).foldLeft(0)(_ + _)
val rightSum = List(1, 2, 3).foldRight(0)(_ + _)
println(leftSum == rightSum) // true
```

But really, why is this that important?. Well, for one, the associative property allows you to break a large list into smaller pieces, and then combine them. The sublists can be run in parallel to improve performance, where necessary.

### Monoid

`Monoid` extends `Semigroup` by adding an identity value

```scala
trait Monoid[A] extends Semigroup[A]:
  def empty: A
```

The empty value is the identity for the combine operation, which means the following is true

```
combine(x, empty) = combine(empty, x) = x
```

This extra property allows us to express a generic combination of Monoid instances

```scala
import cats.kernel.Monoid

def combineMonoids[A: Monoid](xs: List[A]): A =
  xs.foldLeft(Monoid[A].empty)(Monoid[A].combine)
```

### Functor

Typeclass that abstracts over type constructors ("Fs") that can be mapped over. For example List, `Option`, `Either`, `Future`, etc.

```scala
trait Functor[F[_]]:
  def map[A, B](fa: F[A](f: A => B): F[B]
```

A `Functor` must obey the following laws:

* Composition: mapping with f and then again with g is the same ans mapping once with the composition of f a g

```
fa.map(f).map(g) = fa.map(f.andThen(f))
```

* Identity: mapping with the identity function is a no-op

```
fa.map(x => x) = fa
```

Functor is used to model a computational effect on a type. For instance, `Option`'s effect abstracts away potentially missing values. This means that we can consider Functor as a way to treat a **single** effect


### Applicative

`Applicative` extend `Functor` with an `ap` and `pure` methods


```scala
import cats.Functor

trait Applicative[F[_]] extends Functor[F] {
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  def pure[A](a: A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
}
```

`Applicative` allows working with **independent** effects, that can be later composed. 


### Monad

A `Monad` is a way to express sequential computations. Many times if you are using a for-comprehension you are working with a `Monad` (sometimes a `Functor`)

For example, to get the cartesian product of two elements

```scala
import cats.Monad
import cats.Monad.ops.*

def cartesianProduct[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
  for
    a <- fa
    b <- fb
  yield (a, b)

val product = cartesianProduct(List(1, 2, 2), List("a", "b", "c"))
println(s"Monad Cartesian Product: $product") // Monad Cartesian Product: List((1,a), (1,b), (1,c), (2,a), (2,b), (2,c), (2,a), (2,b), (2,c))
```

### ApplicativeError / MonadError

An `ApplicativeError` extends `Applicative` to handle errors. The `ApplicativeError` is defined as follows:

```scala
trait ApplicativeError[F[_], E] extends Applicative[F]:
  def raiseError[A](e: E): F[A]
  def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
  def handleError[A](fa: F[A])(f: E => A): F[A]
  def attempt[A](fa: F[A]): F[Either[E, A]]
```

This allows us to generalize erorr handling in a similar way to `Either`, but for an abstract effect. The only thing we know about `F` is that it is an `Applicative`

There is a corresponding `MonadError` that is an extension of a `Monad`


### Traverse

This typeclass allows us to use `Applicatives` to iterate over a data structure 

```scala
trait Traverse[F[_]]:
  def tragerse[G[_]: Applicative, A, B](fa:F[A])(f: A => G[B]): G[F[B]]
```


Example with `Future`:

```scala
import cats.implicits.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.concurrent.Await

case class User(id: String, name: String)

object UserRepository:
  def users: List[User] =
    List(
      User("a", "John"),
      User("b", "Mary"),
      User("c", "Bob"),
      User("d", "Benjamin")
    )

val userIds = List("a", "b", "c")

def getUser(userId: String): Future[Option[User]] =
  val user = UserRepository.users.filter(u => u.id == userId).headOption
  Future.successful(user)

// To get a all users from the list of Ids
val users = userIds.traverse(getUser)

// NEVER do Await in your code!!!!
println(Await.result(users, 2.seconds)) // List(Some(User(a,John)), Some(User(b,Mary)), Some(User(c,Bob)))
```

### Monad Transformers

Monad Transformers "stack" effectful computations expressed with Monads. 

For example, when retrieving a user from a database, it's very typical to express an API like this:

```scala
def getUser(id: String): F[Option[User]]
```

Where `F` is for example a `Future`. Our method is expressing that maybe there is a user, maybe not. We can rewrite it this way:

```scala
def getUser(id: String): OptionT[F, User]
```

Again `F` here is a higher-kinded type that expresses some effecttul computation. For example, `OptionT[F[_], A]` allows us to compose the monadic properties of `Option` with any other `F[_]` such as a `List`

A Monad Transformer has some very useful methods to wrap and unwrap values from an option, no matter what `F` we are using. When we stack a Monad Transformer on a regular `Monad`, the result is another `Monad`, with the same properties. With this transformed `Monad` we can avoid nested calls to `flatMap`, allowing the use of simpler for-comprehensions

This gets a little confusing, but an `OptionT[List, A]` is the same thing as `List[Option, A]`, and it just provides convenient methods to operate with them


  
## [Practical Example: Functional Validation with Cats](https://github.com/jmarin/cats-validation-example)