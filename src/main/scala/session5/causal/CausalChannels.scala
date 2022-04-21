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


class CausalChannels(p: ProcessConfig)
  extends ReactiveProtocol(p, "causal channels (Raynal-Schiper-Toueg)")
{
  import CausalChannels._

  var sent      = Array.fill(N,N)(0)
  var delivered = Array.fill(N)(0)

  var pending   = Set.empty[CausalMessage]

  private def immutableCopy(a: Array[Array[Int]]) = a.map(_.toIndexedSeq).toIndexedSeq

  def onSend = {
    case s : Signal  => SEND(s)
    case m : Message =>
      SEND(CausalMessage(m, immutableCopy(sent)))
      for (pj <- m.destinations ; (i,j) = (me.value, pj.value)) {
        sent(i)(j) = sent(i)(j) + 1
      }
  }

  listenTo(classOf[CausalMessage])

  def onReceive = {
    case m : CausalMessage =>
      pending += m
      deliverPendingMessages()
  }

  def deliverPendingMessages(): Unit =
  {
    while (pending.exists(m => canDeliver(m))) {
      val m  = pending.find(m => canDeliver(m)).get
      val pj = m.from
      val (i,j) = (me.value, pj.value)
      if (me == ALL.max) {
        println(s"--- from ${pj.name}")
        println ("delivered = " + delivered.mkString("["," ","]"))
        println ("sent = " +
                 sent
                   .map(_.mkString("["," ","]"))
                   .mkString("["," ","]"))
        println ("m.sent = " +
                 m.sent
                   .map(_.mkString("["," ","]"))
                   .mkString("["," ","]"))
      }
      pending -= m
      sent(j)(i)   += 1
      delivered(j) += 1
      for ( px <- ALL ; py <- ALL ; (x,y) = (px.value, py.value) ) {
        sent(x)(y) = sent(x)(y) max m.sent(x)(y)
      }
      DELIVER(m.payload)
    }

  }

  def canDeliver(m: CausalMessage): Boolean =
    ALL.forall(pk => delivered(pk.value) >= m.sent(pk.value)(me.value))
}

object CausalChannels
{
  case class CausalMessage(payload: Message, sent: IndexedSeq[IndexedSeq[Int]]) extends Wrapper(payload)
}
