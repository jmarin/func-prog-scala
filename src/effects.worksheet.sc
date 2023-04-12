import $dep.`org.typelevel::cats-core:2.8.0`
import $dep.`org.typelevel::cats-effect:3.4.8`

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import java.time.Instant

def printTime: IO[Unit] = IO.println(Instant.now().toString())

val f =
  (for {
    _ <- IO.println("Starting...")
    _ <- printTime
    _ <- IO.println("Completed...")
  } yield ())

f.unsafeRunSync()

//
