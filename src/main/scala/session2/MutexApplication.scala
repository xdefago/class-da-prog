/*
 * Copyright (c) 2019, Xavier Defago (Tokyo Institute of Technology).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package session2

import neko._
import neko.util.{MutexClient, Time}


class MutexApplication(p: ProcessConfig)
  extends ActiveProtocol(p, "TrivialMutex") with MutexClient
{
  private val indent = "      " * p.pid.value

  override def run() {
    var visitCount = 50

    println(s"${me.name}: starting")
    while (visitCount > 0) {
      sleep(Time.millisecond * -1000 * math.log(scala.util.Random.nextDouble()))
      // not critical
      println(s"${me.name}: request")
      request()
      println(s"${me.name}: waiting")

      Receive {
        case MutexClient.CanEnter =>
          visitCount -= 1
          println(s"$indent  >> ${me.name}: Enter CS")
          sleep(Time.millisecond * 1000)
          /*
           * CRITICAL SECTION
           */
          println(s"$indent  >> ${me.name}: Release")
          release()
          println(s"${me.name}: left CS")

        case _ => assert(false)
        /* ignore other messages */
      }
    }
  }
}
