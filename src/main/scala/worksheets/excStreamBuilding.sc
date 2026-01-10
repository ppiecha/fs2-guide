import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

import scala.annotation.tailrec

def repeat[F[_], O](s: Stream[F, O]): Stream[F, O] = s ++ repeat(s)

repeat(Stream(0, 1)).take(6).toList

def drain[F[_], O](s: Stream[F, O]): Stream[F, Nothing] = s.flatMap(_ => Stream.empty)

Stream(1,2,3).drain.toList

Stream(1)

def exec[F[_]](fa: F[Unit]): Stream[F, Nothing] = Stream.eval(fa).drain

exec(IO.println("!!")).compile.toVector.unsafeRunSync()

def attempt[F[_], O](s: Stream[F, O]): Stream[F, Either[Throwable, O]] =
  s.flatMap(o => Stream.emit(o.asRight[Throwable])).handleErrorWith(e => Stream(e.asLeft[O]))

attempt((Stream(1,2) ++ Stream(3).map(_ => throw new Exception("nooo!!!")))).toList