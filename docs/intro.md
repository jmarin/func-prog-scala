## Introduction to Functional Programming
   
   * Why Functional Programming
   * Pure functions
   * Higher-order Functions
   * Functional Data Structures
   * Compositionality
   * Side Effects
   * Referential Transparency
   * Polymorphism through Higher-Kinded Types
   * Handling errors without Exceptions (Option, Try, Either data types)
   * Laziness and memoization

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


---

### Higher-order Functions


---

### Functional Data Structures


---

### Compositionality


---

### Side Effects

```scala
val message = "Hello Scala"
println(message)
```

---

### Referential Transparency

---

### Polymorphism through Higher-Kinded Types

What the F[_]

---

### Handling Errros without Exceptions

* Option[A]

* Try[A]

* Either[E, A]

---

### Laziness and Memoization




