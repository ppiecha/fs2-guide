import fs2.Stream

val s0 = Stream.empty
val s1 = Stream.emit(1)
val s1a = Stream(1, 2, 3)
val s1b = Stream.emits(List(1, 2, 3))

s1.toList
s1.toVector

(Stream(1, 2, 3) ++ Stream(4)).toList