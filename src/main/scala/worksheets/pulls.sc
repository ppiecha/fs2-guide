import fs2._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*

val helloStream = Stream("hello")
val helloStreamPull = helloStream.pull.echo
val helloPull = Pull.output1("hello")
val helloPullStream = helloPull.stream
val helloResult = Pull.pure("hello")
val uncons1 = helloStream.pull.uncons1
val pullDone = Pull.done

def take1: Pipe[Pure, String, String] = { in =>
  in.pull.uncons1.flatMap {
    case Some((h, _)) => Pull.output1(h) // Ⓐ
    case None => Pull.done // Ⓑ
  }.stream
}

helloStream.through(take1).compile.toList
Stream.empty.through(take1).compile.toList
Stream("hello", "world").through(take1).compile.toList

helloStream.pull.take(1)