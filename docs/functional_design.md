## Functional Design

   * [Error Handling](#error-handling)
   * [FP Design Principles](#fp-design-principles)
   * [Functional Programming and Domain Driven Design(DDD)](#functional-programming-and-domain-driven-design-ddd)
   * [Clean Software Architecture for FP](#clean-software-architecture-for-fp)
   * [Dealing with Side Effects](#dealing-with-side-effects)
   * [Tagless Final Encoding](#tagless-final-encoding)
   * [Designing Functional Domain Models](#designing-functional-domain-models) 

---

### Error Handling

Functional Programming is like writing a series or algebraic equations (more on that later), and in Algebra you don't use null values or throw exceptions, so you don't use those when writing FP code. One of the main advantages of a functional language like `Scala` is that there are no null values or exceptions. This statement surprises `Java` developers that are new to `Scala`, but it holds true: In `Scala`, there are no `NullPointerException` errors (of course, they can still appear when using `Java` libraries). This doesn't mean that programs cannot fail, so we need a way to model errors and exceptions in our code. There are three main categories of errors: absence of values, operations that can fail, and 

* `Option`: modeling absence of values ("null")

This is modeled in Scala with the `Option` type. This is a type constructor, `Option[A]` where `A` is some other type in your code. It can take two values: 
- `None` represents the absence of a value. This is a type safe way to represent null values, but without the baggage that null has. It is a sound type that can be combined with other types (it is an `Option`)
- `Some` is the monadic wrapper around the value that is expected (A). If the value exists, we return a wrapper that encapsulates it. 

These two flavors of `Option` allow us to do flatMap, for-comprehensions, pattern matching, etc. in our code, following a seamless FP approach to data transformations and manipulation, even if the values are absent. There is one downside to using Option, and that is that it swallows the error if one exists, it is not returned to the user. 

For example, in the following code we know that we get a `None` when the divisor is zero, but no error is communicated back to the user. 

```scala
def divide(a: Int, b: Int): Option[Int] = 
 if b == 0 then
   None
 else
   Some(a / b)
```

* `Try`: modeling things tha can fail (with exceptions)

This is a clean way to handle exceptions, and this type is used a lot when interfacing with `Java` libraries to improve their ergonomics. Try also has two possible values: 

- `Success`: a wrapper around the successful value. If the computation completes without error or exceptions, we wrap the value into a monadic wrapper that represents that it completed successfully. 
- `Failure`: a wrapper that carries some information about the reason for why this computation failed. 

```scala
import scala.util.Success
import scala.util.Failure
import scala.util.Try

def divide(a: Int, b: Int): Try[Int] =
  if b == 0 then 
    new Failure(throw new ArithmeticException("Divide by zero is not allowed"))
  else 
    Success(a / b)
```

* `Either`: Exception-less error handling

`Either` is a type that represents two mutually exclusive values, one being an error and the other one being a successful copmutation

- `Left`: by convention, this is the error value
- `Right`: successful computation

Both are monadic wrappers around the types that Either represents. This constructor type has the following generic signature

`Either[E, A]` where `E` represents the error type and `A` represents the result

`Either` allows us to represent any _domain errors_ in a very concise way, adding logic to our functional APIs when we need to convey to the user that a certain computation should be treated as an error. 

Tip #1: don't use generic types for domain errors. Domain errors should be types that reflect your domain (i.e. `InvalidUserInput` as opposed to something like a `Failure`)

Tip #2: as we saw in previous examples, there are more advanced ways to perform validation on values that encapsulate the notion of an "error", which allow combining errors through an _Applicative Functor_. In the `Cats` library, one such type was `Validated`. We've included here only the types that come with the standard library but keep in mind that depending on the use case, more elaborate types might be useful. 

---

### FP design principles

* **Purity**

A Pure function is a function that always returns the same output when called with the same arguments. This is a function with no side effects. Functions that are pure lead to programs that are referentially transparent, that is, they can be replaced with its corresponding value without changing the program's behavior. This allows for more robust, easier to reason about and maintain programs (with less bugs, too!)

* **Immutability**

This property means that once you assign a value to something, it cannot be changed. Immutability is a core principle of functional programming, and it eliminates a whole class of issues related to state management. This approach makes it much easier to reason about concurrent and / or parallel programs. If a function is immutable it means that it cannot be changed "from the outside", and thus it is a lot easier to reason about what it is doing. 


* **Disciplined State**

AVOID SHARED MUTABLE STATE!!!. If there is one principle to follow, this is it. Shared mutable state cannot be reasoned about easily, is the source of most software bugs out there, and is very hard to maintain. State should be local to your function, and the Single Writer Principle should be observed (a good example of this is Akka's Actor system with its Persistent Actors)

* **Fist class functions and Higher-order functions**

Functions in FP are first-class citizens. They can be passed around as parameters or become the return type of another function. They can be stored inside data structures or assigned to variables. A _Higher-order function_ is a function that takes one or more functions as arguments or returns a function as a result (i.e. `map`, `flatMap`, etc)

* **Type Systems**

A type systems help the compiler in making sure that you have the right types as arguments, return types, etc. A strong type system removes a whole class of bugs from your consideration, which means you have to write less tests. The compiler prevents you from making small but frequent (and sometimes very costly) mistakes. Some compilers like the `Scala` or `Rust` compilers are more advanced and will prevent you from introducing more advanced bugs. Another benefit of a strong type system is that you have implicit documentation, by reading the type signature of your functions you _know_ what the program is doing, or what is allowed in your function. 

* **Referential Transparency**

An expression is said to be referentially transparent if it can be replaced with its corresponding value without changing the program’s behaviour.

This requires functions to be pure. Referential Transparency is a desired trait, because it allows for better composition, from small pieces to a large system. Pure functions can be heavily optimized by the compiler, making your code faster. In essence, your code is faster, easier to read and maintain, and has less bugs. 

---

### Functional Programming and Domain Driven Design (DDD)


---

### Clean Software Architecture for FP

- Bounded Contexts

---

### Dealing with Side Effects

---

### Tagless Final Encoding

---

### Designing Functional Domain Models


#### Algebras

#### Interpreters


