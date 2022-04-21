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
package session5.total_order

import neko._


object Skeen
{
  class Client(p: ProcessConfig) extends ReactiveProtocol(p, "TObcast (Skeen)")
  {
    private case class Info(count: Int, maxDate: Long)

    private var tsCount = Map.empty[Long,Info].withDefaultValue(Info(0, Long.MinValue))
    private var seqNum: Long = 0


    def onSend = {
      case m : Message if m.destinations == ALL =>
        seqNum += 1
        SEND(Inquiry(m, seqNum))

      case o => SEND(o)
    }


    listenTo(classOf[PropDate])

    def onReceive = {
      case PropDate(_, _, sn, date) =>
        val info    = tsCount(sn)
        val maxDate = info.maxDate max date

        if (info.count + 1 < N) {
          tsCount = tsCount.updated (sn, Info (info.count + 1, maxDate))
        }
        else {
          tsCount -= sn
          SEND(FinalDate(me, ALL, sn, maxDate))
        }
    }
  }


  class Server(p: ProcessConfig) extends ReactiveProtocol(p, "TObcast (Skeen)")
  {
    private case class Record(m: Message, date: Long, sender: PID, sn: Long, canDeliver: Boolean)

    private var clock_i: Long = 0
    private var pending_i     = Map.empty[(PID,Long), Record]


    def onSend = PartialFunction.empty

    listenTo(classOf[Inquiry])
    listenTo(classOf[FinalDate])

    def onReceive = {
      case Inquiry(m, sn) =>
        val pi = m.from
        clock_i += 1
        pending_i += (pi, sn) -> Record(m, clock_i, pi, sn, canDeliver = false)
        SEND(PropDate(me, pi, sn, clock_i))

      case FinalDate(pi, _, sn, date) =>
        pending_i
          .get((pi,sn))
          .fold {
            throw new Exception(s"ERROR: got final date for an unknown message: (${pi.name},$sn)")
          } { rec =>
            val nrec = rec.copy(date = date, canDeliver = true)
            pending_i = pending_i.updated((pi,sn), nrec)
            clock_i   = clock_i max date
          }
        deliverWhatCan()
    }


    private def isDeliverable(rec: Record): Boolean =
      rec.canDeliver &&
        pending_i.values
          .filterNot(r => r.sender == rec.sender && r.sn == rec.sn)
          .forall { r =>
            rec.date < r.date ||
              (rec.date == r.date && rec.sender < r.sender) ||
              (rec.date == r.date && rec.sender == r.sender && rec.sn < r.sn)
          }

    private def deliverWhatCan(): Unit =
    {
      while (true) {
        pending_i
          .find(kv => isDeliverable(kv._2))
          .fold {
            return
          } { kv =>
            val (key, rec) = kv
            pending_i -= key
            DELIVER(rec.m)
          }
      }
    }
  }


  case class Inquiry(m:Message, sn: Long) extends Wrapper(m)
  case class PropDate(from: PID, to: PID, sn: Long, date: Long)
    extends UnicastMessage
  case class FinalDate(from: PID, to: Set[PID], sn: Long, date: Long)
    extends MulticastMessage
}
