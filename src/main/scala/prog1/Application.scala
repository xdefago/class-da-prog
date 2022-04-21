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

package prog1

import neko._

class Application(p: ProcessConfig, root: PID = PID(0), data: BroadcastConvergecast.DataType)
  extends ReactiveProtocol(p, "pif app")
{
  import BroadcastConvergecast._

  override def preStart() {
    println(s"${me.name} starting...")
    if (me == root) SEND( START(data) )
  }

  listenTo(classOf[TERMINATE])
  listenTo(classOf[VISIT])

  override def onReceive= {
    case VISIT(data) =>
      println(s"${me.name} received: $data")

    case TERMINATE(values) =>
      values
        .toList
        .sortBy(_._1)
        .foreach { kv =>
          val (pid, value) = kv
          println(s"${pid.name}: children = " + value.map(_.name).mkString("{", ", ", "}"))
        }
  }

  override def onSend = PartialFunction.empty
}

