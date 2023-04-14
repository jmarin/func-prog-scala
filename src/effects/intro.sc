//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.8"

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import java.time.Instant
import scala.concurrent.*
import scala.concurrent.duration.*
import concurrent.ExecutionContext.Implicits.global

def printTime: IO[Unit] = IO.println(Instant.now().toString())

val io =
  (for {
    _ <- IO.println("Starting Cats Effect...")
    _ <- printTime
    _ <- printTime
    _ <- IO.println("Completed Cats Effect...")
  } yield ())

io.unsafeRunSync()

//
