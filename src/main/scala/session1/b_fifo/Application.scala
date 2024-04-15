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

package session1.b_fifo

import neko._

class Application (p: ProcessConfig, numIter: Int)
  extends ActiveProtocol(p, "App")
{
  private var receivedFrom = Map .empty[PID, List[Long]] .withDefaultValue(Nil)

  listenTo(classOf[Application.GenericMessage])

  override def run (): Unit =
  {
    for (i <- 1 to numIter) {
      println(s"Application send... ${me.name} sequence number is $i")
      SEND(Application.GenericMessage(me, neighbors, i))
    }

    for (_ <- 1 to numIter) {
      for (_ <- neighbors) {
        Receive {
          case m @ Application.GenericMessage(from,_,sn) =>
            receivedFrom = receivedFrom.updated(from, sn :: receivedFrom(from))
            if (me == PID(0)) {
              println(s"[${me.name}] message received: $m")
            }

          case _ =>
        }
      }
    }

    println(s"[${me.name}] FINISHED")
    neighbors.foreach { p =>
      val arrivalSequence = receivedFrom(p).reverse.mkString(" ")
      println(s"[${me.name}] from ${p.name} -> $arrivalSequence")
    }
  }
}


object Application
{
  case class GenericMessage(from: PID, to: Set[PID], sn: Long)
    extends MulticastMessage
}
