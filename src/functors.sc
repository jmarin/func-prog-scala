object Functors:

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

import Functors.*

val list = List(1, 2, 3)
println(Functors.multiplyBy10(list))

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
