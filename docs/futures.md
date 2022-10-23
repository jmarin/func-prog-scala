---
title: Futures
author: Juan Marin Otero <juan.marin.otero@gmail.com>
extensions:
  - image_ueberzug
---

## Back to the Future: Concurrency and Parallelism

  * [Amdahl's Law](#amdahls-law)
  * [The JVM threading model](#the-jvm-threading-model)
  * [Event Loops](#event-loops)
  * [Asynchronous and non-blocking I/O with Futures](#asynchronous-and-non-blocking-io-with-futures)
  * [Concurrency vs Parallelism](#concurrency-vs-parallelism)
  * [Interacting with Akka Actors](#integrating-with-akka-actors)
  * [Future Patterns: use cases and practical examples](#future-patterns-use-cases-and-practical-examples)
  * [Testing Futures](#testing-futures)
  * [Issues with the Future API](#issues-with-the-future-api)  




---

### Amdahl's Law

![40](img/AmdahlsLaw.png)

This law refers to the theoretical speedup in latency of the execution of a task is a function of the number of processors executing it. The speedup is limited by the serial part of the program. For example, if 95% of the program can be parallelized, the theoretical maximum speedup using parallel computing is 20 times. For a program with 50% of its code running in parallel, the maximum number of cores that come into play is 16, and the maxium speedup possible is 10x. 

---



### The JVM threading model

The Java Virtual Machine (JVM) uses threads as the core concurrency primitive to express multi-tasking. Threading on the JVM uses native operating system threads. Native threads are very efficient, as they are closer to the hardware, but they come at a cost as they are expensive resources and take . 

Any time we use `java.lang.Thread`, `java.util.concurrent.Executor`, `java.util.concurrent.ExecutorService` etc. we are using native threads, that are translated to operating system threads.

One alternative to native threads is _green threads_, which don't map directly to native hardware threads (but still use them). This usually works by mapping M green threads to N native threads, where M > N. This mechanism is quite complicated but is being adopted by many libraries and languages across the industry. Languages like `Go`, `Kotlin` have implementations of concurrency primitives similar to green threads; in Scala, libraries like `Cats-Effect` or `ZIO` also have something similar through the implementation of _fibers_ (hold that thought; we will revisit this term later). 

For long running tasks, native threads are more efficient. For very short workloads (and many more of them) green threads offer a significant performance advantage. Up until recently the JVM didn't support the concept of green threads, but the release of JDK19 (just released at the time of this writing) will change that. Green threads are now supported directly on the JVM runtime through project `Loom`, which will change many of the concurrent libraries we use. But that is a story for another day, that will take some time to unfold. 

The bottom line is that in its current form, the `JVM` uses native threads, which are very efficient but must be treated as a scarce resource that needs to be carefully managed in order to extract the maximum and most efficient performance from our systems.  

In contrast, let's take a very brief look at the threading model for `JavaScript` and `Python`. In the case of `JavaScript`, concurrency is achieved by leveraging event loops (see below). `Python` offers some options, but none of them are optimal due to lack of native support for a multithreaded runtime and they feel very primitive and cumbersome when compared to a real multithreaded language (which is usually also compiled and thus further optimized). `Python` threads are cooperative, which means that the runtime divides its attention between them. This approach is not recommended for CPU intensive work (i.e. calculating hash functions). The other way in which `Python` can execute functions concurrently is by using `async`, which also uses cooperative multitasking on a single thread. This means there is no real support for parallelism. Also, Python as a runtime is notoriously slow (don't take my word for it; look up some of the benchmarks out there).

In contrast, the `JVM` threading model allows for both concurrency and parallelism within the same application, if necessary, as we will see below.


---

### Event Loops

Event Loops are a model for concurrency that has become very popular for certain runtimes. This is how `Node.js` works, and it has also been utilized in certain `Java` libraries that required very high levels of concurrency (think high frequency trading as a use case, for example). This approach has been used in many cases to build frameworks for scalable network services and high-performance web applications. Most of these solutions rely on a single thread of execution, with a single event queue and a single event loop responsible for handling incoming requests. They require asynchronous, non-blocking I/O to function, and can still achieve very good performance. Event loops are useful for I/O bound workloads. If a task requires expensive computations, it usually gets spawned to another process to execute, but this is far from optimal. 

![40](img/eventloop.jpeg)

(image credit [here](https://medium.com/@ktachyon/javascript-concurrency-model-dc98dacab527))

The problem with event loops is that if the main thread blocks, the whole application deadlocks and stops functioning. It is very important that the main thread of execution is only responsible for processing asynchronous, non-blocking I/O operations. The execution of callbacks in this model makes absolutely no guarantee for ordering, which needs to be taken into account in the programming model. 

`JavaScript` is particularly well suited to this type of concurrency through `Node.js`. It works well for I/O bound workloads. It is a very suboptimal approach if you have computationally intensive workloads.

---

### Asynchronous and non-blocking I/O with Futures

A `Future` is a placeholder for a value that may not yet exist. They generally represent concurrent or parallel execution of code, in different threads. If we add non-blocking properties and thanks to their compositional nature we have the capability to create faster, **asynchronous**, **non-blocking** parallel code.  

By default `Future` and `Promise` are non-blocking, making use of callbacks instead of blocking code. The standard library offers operations such as `flatMap`, `foreach`, `filter`, `map`, `traverse`, etc in order to compose futures in a monadic way. Blocking is possible but it shouldn't be used unless necessary, as it can lead to poor performance and deadlocks.

A `Promise` is a writable container that completes with a `Future` (once). A Promise completes or fails a future by calling its corresponding `success` or `failure` methods

#### ExecutionContext

Any `Future` is going to require an `ExecutionContext` to run, which is usually provided _implicitly_. This provides information about the thread pool available and its properties, so that the `Future` can run along with other concurrent and / or parallel code. 

The most basic `ExecutionContext` is the `global` one, available by importing as follows:

```scala
import scala.concurrent.ExecutionContext.Implicits.global
```

This `ExecutionContext` is backed by a `ForkJoinPool`, whis should be a good starting point but has to be handled with care. A `ForkJoinPoll` manages a group of threads, the number of which is designed by the `parallelism` property. The maximum number of blocking calls that it can process is equal to this setting, and if reached it could starve the pool of threads. By default, the global `ExecutionContext` creates a pool with the number of threads equal to the number of available processors. 

If we have more blocking calls than available threads we can use the `blocking` code:

```scala
Future {
    blocking {
        // blocking code
    }
}
```

Important: `ForkJoinPool` is fine for most use cases, but not for blocking calls that take a long time. For those cases it's best to use a dedicated `ExecutionContext`.

Real world workloads are usually a mix of blocking and non-blocking code. The best approach is to separate these operations into separate thread pools, optimized for each type of work load (i.e. async I/O vs blocking database or file access). Runtimes like `Akka` or `Cats-Effect` come with sane defaults but also allow you to specify the type of thread pool to use in each case. 


[Example: Asynchronous computation, interacting with Actors](https://github.com/jmarin/scala-futures-examples)


---

### Concurrency vs Parallelism

_Concurrency_ and _Parallelism_ refer to two related, but distinct concepts. Thery are used interchangeably very frequently, causing confusion. 

_Concurrency_ refers to several different tasks executing at the same time, independent of each other, with no particular order of execution. 

_Parallelism_ is about multiple tasks or subtasks running at the same time with multiple cores or CPUs. 

Parallelism is at the hardware level, it requires the physical cores to be present and the software to take advantage of them. Concurrency is a system property, independent of the physical hardware layer. Concurrency creates the _illusion_ of parallelism, usually by context switching at the CPU level. When applying Parallelism, we are subdividing a task into smaller chunks, and running them in parallel; and maybe later joining some results from those subtasks. this is whey Amdahl's Law is so important; it's rare to find an algorithm that works on real world data that is 100% parallel.

For example, in `Akka` the Actor Model is a programming model specifically designed for concurrency (and it is a mistake to use it for parallel workloads). The `Future` API can be used for both:

* Concurrency: by default, a `Future` will run in a thread if it is available (using thread pools, as we will see later). Once we declare a `Future`, that code runs in a different thread than the main program. This API offers several ways to deal with how to process those responses:

    - You can use monadic expressions to chain processes, or transform the wrapped result inside a `Future`

    ```scala
    for 
      f <- userRepository.getUser(id) // Gets user from database, returns a Future[Option[User]]
      p <- authRepository.getPermissions(f.map(_.id)) // Gets user permissions from external service, returns Future[Permissions]
    yield p
    ```

   In cases like this, you are still deferring the "unwrapping" of the value from the `Future` to the runtime or another part of the program.

   - If you need to retrieve the value, the `Future` API allows you to return the computation by blocking the thread and awaiting for a response. **NEVER DO THIS IN PRODUCTION CODE**, especially one that is meant to perform under load. This option blocks the thread of execution, which are expensive resources. It is unnecessary. In a testing environment, it can lead to contention and flaky tests that time out for no apparent reason. 

   ```scala
   val f = Future(...) // Some computation that returns a Future
   val result = Await.result(f, 3.seconds) // Block the thread for up to 3 seconds, give me result
   println(result) // Print result
   ```

   - Use callbacks to process the result, once it is available. This is the preferred method, by leveraging pattern matching

   ```scala
   val f = Future(...) // Some computation that returns a Future
   f.onComplete {
      case Success(value) => println(value)
      case Failure(ex) => println(ex.getLocalizedMessage())
   }
   ```

---

### Interacting with Akka Actors

`Akka` Actors provide a mechanism to express concurrency, and have some interesting properties to model several types of computation. Their properties are outside of the scope of this material, but one of them is that Actors can only communicate with each other by passing messages. So how can we use them "outside" of an Actor System?. In Akka, this is solved by a pattern that specifies how to implement a request/response interaction with an Actor, from the outside. The way this is done is that the `Akka` Actor returns a `Future`

```scala
val u: Future[User] = actorRef.ask(ref => GetUser(ref))
```

In the above code, the `actorRef` is an Actor Reference. How to get that is out of scope, but you can read more in the `Akka` introductory material to learn how to do just that. The `ref` refers to the entity that the Actor replies to, and it will be encoded in this case as `ActorRef[User]`. This is the type of message that another actor would expect. The Akka toolkit allows you to transform this to a Future with the Ask pattern, which requires an implicit timeout to be in scope.

With this technique, you can build client libraries or programs that interact with Actor Systems, as long as you can instantiate them through an `ActorSystem`. It is very common in these scenarios to use _for-comprehension_ semantics to call many different actors that perform different functions. 


---

### Future Patterns: use cases and practical examples

[Example Project](https://github.com/jmarin/scala-futures-examples)

---

### Testing Futures

The most important thing: **DO NOT BLOCK YOUR FUTURES FOR A RESPONSE!!!**

If you are using `ScalaTest`, you can include the asynchronous Spec classes or traits, that allow execution of Future based code in your tests. Additionally, adding the [ScalaFutures](https://www.scalatest.org/scaladoc/3.0.6/org/scalatest/concurrent/ScalaFutures.html) trait allows you to program in a way that looks imperative and blocking, but isn't. 

```scala
class FutureSpec extends AsyncWordSpec with Matchers with ScalaFutures

"A future" should {
  "return correct value of 1" in {
    f.futureValue shoudlBe 1
  }
}
```

---

### Issues with the Future API

There is a problem in the `Future` in the `Scala` standard library, because of these two properties:

- Eager evaluation: `Future` starts evaluating its value right after it's defined
- Memoization: once the value is computed, it's shared with other calls, without being recalculated. 

**This means that Future is not referentially transparent**

The problem is that the `Future` doesn't describe a computation, it just........executes it. This violates the nice properties that we get with referentially transparent functions, where we can pass functions around and reason about our whole system in a consistent way.

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

**With Futures, our program acts differently depending on where we write our code**

Another problem with `Future` is that because of its eager execution mode it requires an `ExecutionContext` to be passed around implicitly everywhere a `Future` needs to run. This pollutes your code with unnecessary contextual abstractions (using / givens in Scala 3, implicits in Scala 2)

