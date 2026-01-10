import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val eff = Stream.eval(IO { println("Being run"); 1 + 1 })
eff.compile
eff.compile.toVector
eff.compile.drain
eff.compile.drain.unsafeRunSync()
eff.compile.toVector.unsafeRunSync()
