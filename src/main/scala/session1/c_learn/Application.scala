/*
 * Copyright (c) 2020, Xavier Defago (Tokyo Institute of Technology).
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

package session1.c_learn

import neko._


class Application(p: ProcessConfig, initiators: Set[PID] = Set(PID(0)))
  extends ActiveProtocol(p, "App")
    with LearnTopologyClient
{
  override def run(): Unit =
  {
    if (initiators contains me) {
      println(s"[${me.name}] STARTING")
      learnStart()
    }
    Receive {
      case LearnTopologyClient.Learn(edges) =>
        println(s"[${me.name}] knows the communication graph.")
        edges
          .toList
          .sorted
          .map(c => (c._1.name, c._2.name))
          .map(c => s"[${me.name}]     $c")
          .foreach(println)
        println(s"[${me.name}]     END")

      case unknown =>
        println(s"[${me.name}] Unknown message: $unknown")
    }
  }
}
