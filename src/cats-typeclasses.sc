import $dep.`org.typelevel::cats-core:2.8.0`

// Semigroup

import cats.kernel.Semigroup

val map1 = Map("one" -> 1, "two" -> 2)
val map2 = Map("three" -> 3, "four" -> 4)

val mapCombined = Semigroup[Map[String, Int]].combine(map1, map2)
println(mapCombined)

val leftSum = List(1, 2, 3).foldLeft(0)(_ + _)
val rightSum = List(1, 2, 3).foldRight(0)(_ + _)
println(leftSum == rightSum)

// Monoid

import cats.kernel.Monoid

def combineMonoids[A: Monoid](xs: List[A]): A =
  xs.foldLeft(Monoid[A].empty)(Monoid[A].combine)

// Monad

import cats.Monad
import cats.Monad.ops.*

def cartesianProduct[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A, B)] =
  for
    a <- fa
    b <- fb
  yield (a, b)

val product = cartesianProduct(List(1, 2, 2), List("a", "b", "c"))
println(s"Monad Cartesian Product: $product")

// Traverse

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
println(Await.result(users, 2.seconds))
