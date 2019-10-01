scala-verify
============

scala-verify is a fork of [Minitest](https://github.com/monix/minitest) + [SourceCode](https://github.com/lihaoyi/sourcecode) to prepare for eventual merge into scala/scala.
The purpose of scala-verify is to create a cross-platform, zero-dependency, minimal, testing framework for bootstrapping the toolchain and a small handful of foundational third-party libraries.
See https://github.com/scala/scala-dev/issues/641.

A mini testing framework cross-compiled for Scala 2.11, 2.12,
2.13, Dotty, [Scala.js 0.6.x](http://www.scala-js.org/) and
[Scala Native 0.3.x](https://www.scala-native.org/).

## Usage in sbt

For `build.sbt` (use the `%%%` operator for Scala.js):

```scala
// use the %%% operator for Scala.js
libraryDependencies += "com.eed3si9n.verify" %% "verify" % "0.2.0" % Test

testFrameworks += new TestFramework("verify.runner.Framework")
```

## Tutorial

### BasicTestSuite

Test suites MUST BE objects, not classes. To create a test suite without `setup` and `teardown`,
extend [BasicTestSuite](shared/src/main/scala/verify/BasicTestSuite.scala) trait:

Here's a simple test:

```scala
import verify._

object SomethingTest extends BasicTestSuite {
  test("addition") {
    assert(2 == 1 + 1)
  }

  test("failing test") {
    case class Person(name: String = "Fred", age: Int = 42) {
      def say(words: String*) = words.mkString(" ")
    }
    assert(Person().say("ping", "poing") == "pong pong")
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    intercept[DummyException] {
      test()
    }
  }
}
```

In the context of `BasicTestSuite`, `assert(...)` function is wired up to power assertion,
as known in Groovy. In the above, the failing test would result to the following:

```
- failing test *** FAILED ***
  assertion failed

  assert(Person().say("ping", "poing") == "pong pong")
         |        |                    |
         |        ping poing           false
         Person(Fred,42)
    verify.asserts.PowerAssert$AssertListener.expressionRecorded(PowerAssert.scala:38)
    verify.asserts.RecorderRuntime.recordExpression(RecorderRuntime.scala:39)
    example.tests.SimpleTest$.$anonfun$new$9(SimpleTest.scala:54)
```

### assertions

For the most part, `assert(...)` should do the job.

To compare large strings, scala-verify also provides `assertEquals(expected: String, found: String, message: => String)`.
In case of an error, it will print the diff in color for easier detection of the differences.

### setup and tearDown

In case you want to setup an environment for each test example and need `setup` and
`tearDown` semantics, per test example, extend [TestSuite](shared/src/main/scala/verify/TestSuite.scala).
Then on each `test` definition, you'll receive a fresh value:

```scala
import verify.TestSuite

object SomethingTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0, "should still be positive")
  }

  test("should be positive") { env =>
    assert(env > 0, "positive test")
  }

  test("should be lower or equal to 100") { env =>
    assert(env <= 100, s"$env > 100")
  }
}
```

Some tests require setup and tear down logic to happen only once per test suite
being executed and `TestSuite` supports that as well, but note you should abstain
from doing this unless you really need it, since the per test semantics are much
saner:

```scala
object SomethingTest extends TestSuite[Int] {
  private var system: ActorSystem = _

  override def setupSuite(): Unit = {
    system = ActorSystem.create()
  }

  override def tearDownSuite(): Unit = {
    TestKit.shutdownActorSystem(system)
    system = null
  }
}
```

### asynchronous testing

scala-verify supports asynchronous results in tests, use `testAsync` and
return a `Future[Unit]`:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

object SomethingTest extends BasicTestSuite {
  testAsync("asynchronous execution") {
    val future = Future(100).map(_+1)

    for (result <- future) yield {
      assert(result == 101)
    }
  }
}
```

"Beauty is truth, truth beauty,—that is all
    Ye know on earth, and all ye need to know."

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [NOTICE](./NOTICE).
