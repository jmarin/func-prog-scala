## Purely Functional I/O


### Introduction

### The IO Monad

Originally implemented in languages like `Haskell`, where it behaves differently and makes more sense. `Haskell` is a pure functional language, and any operation that deals with I/O will have a return type of `IO` (in addition to this, there are other restrictions on how it gets used). In `Scala` this is obviously not the case, and we have to use libraries in order to implement similar behavior.  

But why would we want to do this? 

First, we saw in previous chapters how the `Future` API had some issues with regards to referential transparency and reasoning of the code. Changing where we write a `Future` changes the way the program behaves.




**Martin Odersky**: _"The IO monad does not make a function pure. It just makes it obvious that it's impure"_