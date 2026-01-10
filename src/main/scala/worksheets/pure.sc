import fs2.Stream

val s0 = Stream.empty
val s1 = Stream.emit(1)
val s1a = Stream(1, 2, 3)
val s1b = Stream.emits(List(1, 2, 3))

s1.toList
s1.toVector

val s1c: fs2.Stream[[x] =>> fs2.Pure[x], Int] = Stream(1, 2, 3)