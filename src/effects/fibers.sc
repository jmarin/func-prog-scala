//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.8"

import cats.effect.IO
import cats.effect.syntax.*
import cats.effect.unsafe.implicits.global // Don't do this in production!!!

import cats.effect.{IO, Fiber}
import scala.concurrent.duration.*

extension [A](io: IO[A])
  def debug: IO[A] = io.map(value =>
    println(s"[${Thread.currentThread().getName}] $value")
    value
  )

// Sleep

// Sleep for 5 seconds and then print a message
val fiber: IO[Fiber[IO, Throwable, Unit]] = IO
  .sleep(5.seconds)
  .flatMap(_ => IO(println("Hello, world!")))
  .debug
  .start
