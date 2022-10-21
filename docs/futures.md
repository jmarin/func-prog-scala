## Back to the Future: Concurrency and Parallelism

  * [Amdahl's Law](#amdahls-law)
  * [Concurrency vs Parallelism](#concurrency-vs-parallelism)
  * [The JVM threading model](#the-jvm-threading-model)
  * [Event Loops](#event-loops)
  * [Asynchronous and non-blocking I/O with Futures](#asynchronous-and-non-blocking-io-with-futures)
  * [Interacting with Akka Actors](#integrating-with-akka-actors)
  * [Future Patterns: use cases and practical examples](#future-patterns-use-cases-and-practical-examples)
  * [Testing Futures](#testing-futures)
  * [Issues with the Future API](#issues-with-the-future-api)  




---

### Amdahl's Law

---

### Concurrency vs Parallelism

---

### The JVM threading model

---

### Event Loops

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

### Interacting with Akka Actors

---

### Future Patterns: use cases and practical examples



---

### Testing Futures

---

### Issues with the Future API
