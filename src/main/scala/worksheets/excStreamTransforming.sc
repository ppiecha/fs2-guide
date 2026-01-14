import fs2.{Chunk, Pipe, Pull, Pure, Stream}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*

import scala.annotation.tailrec

def takeWhile[F[_], O](stream: Stream[F, O], p: O => Boolean): Stream[F, O] = {
  // def scanChunksOpt[S, O2 >: O, O3](init: S)(f: S => Option[Chunk[O2] => (S, Chunk[O3])]): Stream[F, O3]
  stream.scanChunksOpt(false) { s =>
    if s then None
    else {
      Some((c: Chunk[O]) => {
        if c.forall(p) then (false, c)
        else (true, Chunk.from(c.takeWhile_(p)))
      })
    }
  }
}

def takeWhileUncons1[F[_], O](stream: Stream[F, O], p: O => Boolean): Stream[F, O] =
  stream.pull.uncons1.flatMap {
    case Some((h, t)) => if p(h) then Pull.output1(h) >> takeWhileUncons1(t, p).pull.echo else Pull.done
    case None         => Pull.done
  }.stream

def takeWhileUncons[F[_], O](stream: Stream[F, O], p: O => Boolean): Stream[F, O] =
  stream.pull.uncons.flatMap {
    case Some((chunk, restOfStream)) =>
      if chunk.forall(p) then Pull.output(chunk) >> takeWhileUncons1(restOfStream, p).pull.echo
      else Pull.output(Chunk.from(chunk.takeWhile_(p)))
    case None => Pull.done
  }.stream

takeWhile(Stream.range(0, 100), _ < 7).toList
takeWhileUncons1(Stream.range(0, 100), _ < 7).toList
takeWhileUncons(Stream.range(0, 100), _ < 7).toList

def intersperseChunk[O](chunk: Chunk[O], separator: O): Chunk[O] =
  if chunk.size <= 1 then chunk
  else chunk.take(1) ++ Chunk(separator) ++ intersperseChunk(chunk.drop(1), separator)

intersperseChunk(Chunk.from(List(1, 2, 3)), 0)

def intersperse[F[_], O](separator: O): Pipe[F, O, O] = {
  def go(stream: Stream[F, O]): Pull[F, O, Unit] =
    stream.pull.uncons1.flatMap {
      case Some((o1, restOfStream1)) =>
        restOfStream1.pull.uncons1.flatMap {
          case Some((o2, restOfStream2)) =>
            // println(s"nested $o1 $o2")
            Pull.output1(o1) >> Pull.output1(separator) >> go(restOfStream1)
          case None => Pull.output1(o1) >> Pull.done
        }
      case None => Pull.done
    }
  in => go(in).stream
}

intersperse("|")(Stream("Alice", "Bob") ++ Stream("Carol")).toList
intersperse("|")(Stream.empty).toList
intersperse("|")(Stream("Alice")).toList
intersperse("|")(Stream("Alice", "Bob")).toList

def scan[F[_], O1, O2](z: O2)(f: (O2, O1) => O2): Pipe[F, O1, O2] =
  def go(z: O2)(f: (O2, O1) => O2)(stream: Stream[F, O1]): Pull[F, O2, Unit] =
    stream.pull.uncons.flatMap {
      case Some((chunk1: Chunk[O1], restOfStream1)) =>
        val chunkScan = chunk1.scanLeft(z)(f)
        restOfStream1.pull.uncons.flatMap {
          case Some((_, _)) =>
            Pull.output(chunkScan.take(chunkScan.size - 1)) >> go(chunkScan.last.get)(f)(restOfStream1)
          case None => Pull.output(chunkScan)
        }
      case None => Pull.done
    }
  in => go(z)(f)(in).stream

scan[Pure, Int, Int](0)(_ + _)(Stream.range(1, 10)).toList // running sum
