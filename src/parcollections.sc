import $dep.`org.scala-lang.modules::scala-parallel-collections:1.0.4`

import collection.parallel.CollectionConverters.*

val numbers = (1 to 1000000).toArray

val mr = numbers.map(_ + 1).reduce(_ + _)

val mrPar = numbers.par.map(_ + 1).reduce(_ + _)

println(s"Sum: $mr")
println(s"Sum Parallel: $mrPar")
