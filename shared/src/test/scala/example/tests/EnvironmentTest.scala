/*
 * Copyright (c) 2014-2019 by The Minitest Project Developers.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.tests

import cutest.TestSuite
import cutest.platform.Future
import scala.util.Random

object EnvironmentTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0)
  }

  override def setupSuite() = {}

  override def tearDownSuite() = {}

  test("simple test") { env =>
    assertEquals(env, env)
  }

  testAsync("asynchronous test") { env =>
    import cutest.platform.ExecutionContext.Implicits.global

    Future(env).map(_ + 1).map { result =>
      assertEquals(result, env + 1)
    }
  }
}