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
package session5.causal

import neko._


class CausalTestApp(p: ProcessConfig) extends ActiveProtocol(p, "causal test")
{
  import CausalTestApp._

  listenTo(classOf[AppMessage])

  def run(): Unit =
  {
    val pmax = ALL.max

    if (me == PID(0)) {
      SEND(AppMessage(me, pmax))
      for (pj <- neighbors-pmax) {
        SEND(AppMessage(me, pj))
      }
    }
    else if (me == ALL.max) {
      var recvSeq = Seq.empty[PID]

      for (_ <- ALL-me) {
        Receive {
          case AppMessage(f,_) =>
            println(s"    ${me.name} GOT from ${f.name}")
            recvSeq = recvSeq :+ f
          case _ =>
        }
      }
      println(s"${me.name} > receive sequence: " + recvSeq.map(_.name).mkString(" "))
    }
    else {
      Receive {
        case AppMessage(_,_) => SEND(AppMessage(me, ALL.max))
        case _ =>
      }
    }
  }
}

object CausalTestApp
{
  case class AppMessage(from: PID, to: PID) extends UnicastMessage
}
