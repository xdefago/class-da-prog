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

import scala.collection.mutable

/**
  * Implements multi-party FIFO channels.
  * The protocol is not thread-safe if each process has more than one send thread
  * or more than one receive thread.
  *
  * @param p
  */
class FIFOChannel (p: ProcessConfig)
  extends ReactiveProtocol(p, "FIFO channels")
{
  import FIFOChannel._

  // TODO: add necessary state variables 

  private val messageBuffer   =
    IndexedSeq.fill(N)(mutable.PriorityQueue.empty[FIFOInfo](FIFOChannel.FIFOOrdering))


  def onSend = {

    case m : Message =>
      // TODO: implement the proper sending of a message
      m.destinations foreach {
        dest =>
          SEND( FIFOSequenced(me, dest, m, 0) )
      }
  }

  listenTo(classOf[FIFOSequenced])

  def onReceive = {

    case FIFOSequenced(from, to, m, sn) =>
      // TODO: implement the **correct** reception of a message
      messageBuffer(from.value) enqueue FIFOInfo(sn, m)
      val buffer = messageBuffer(from.value)
      val info   = buffer.dequeue()
      DELIVER(info.msg)

    case _ => /* IGNORE */
  }
}


object FIFOChannel
{
  case class FIFOSequenced(from: PID, to: PID, payload: Message, seqNum: Long)
    extends UnicastMessage

  case class FIFOInfo (seqNum: Long, msg: Message)
  {
    def from: PID = msg.from
  }

  val FIFOOrdering =
    Ordering[(Long, Int)]
      .on((info: FIFOInfo) => (-info.seqNum, info.from.value))
}
