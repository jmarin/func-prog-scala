//> using dep "org.typelevel::cats-core:2.9.0"
//> using dep "org.typelevel::cats-effect:3.4.8"

import cats.effect.IO
import cats.effect.unsafe.implicits.global // Don't do this in production!!!

val io1: IO[Int] = IO(42)
val io2: IO[String] = IO("Meaning of life")

val meaningStr: IO[String] =
  io1 *> io2 // computes both, io1 first io2 second, keeps io2 result
val meaningStr2: IO[Int] =
  io1 <* io2 // computes both, io1 first io2 second, keeps io1 result

val io = (for
  r1 <- meaningStr
  _ <- IO.println(r1)
yield ())

io.unsafeRunSync()
