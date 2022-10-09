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
println(m3)
