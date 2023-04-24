//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.9"

import cats.effect.{IO, Fiber}
import scala.concurrent.duration.*
import cats.effect.unsafe.implicits.global

val program: IO[Unit] =
  for
    fiber <- IO.println("hello!").foreverM.start
    _ <- IO.sleep(5.seconds)
    _ <- fiber.cancel *> IO.println("Fiber Canceled!")
  yield ()

program.unsafeRunSync()
