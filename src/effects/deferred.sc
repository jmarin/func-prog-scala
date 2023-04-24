//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.9"

import cats.effect.IO
import cats.effect.kernel.Deferred
import cats.syntax.all.*
import scala.concurrent.duration.*
import cats.effect.unsafe.implicits.global

def countdown(n: Int, pause: Int, waiter: Deferred[IO, Unit]): IO[Unit] =
  IO.print(s"$n ") *> IO.defer {
    if (n == 0) then IO.unit
    else if (n == pause) then
      IO.println("paused....") *> waiter.get *> countdown(n - 1, pause, waiter)
    else countdown(n - 1, pause, waiter)
  }

val program: IO[Unit] =
  for
    waiter <- IO.deferred[Unit]
    f <- countdown(10, 5, waiter).start
    _ <- IO.sleep(5.seconds)
    _ <- waiter.complete(())
    _ <- f.join
    _ <- IO.println("blast off!")
  yield ()

program.unsafeRunSync()
