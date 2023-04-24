//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.9"

import cats.effect.IO
import cats.effect.Fiber
import cats.effect.unsafe.implicits.global

val program: IO[Unit] =
  for
    // Start the first fiber
    fiber1 <- IO {
      println("Fiber 1 started")
      Thread.sleep(1000)
      println("Fiber 1 finished")
    }.start

    // start the second fiber
    fiber2 <- IO {
      println("Fiber 2 started")
      Thread.sleep(2000)
      println("Fiber 2 finished")
    }.start

    // Join the fibers
    _ <- fiber1.join
    _ <- fiber2.join

    // Print a message when both fibers are finished
    _ <- IO(println("Both fibers are finished!"))
  yield ()

program.unsafeRunSync()
