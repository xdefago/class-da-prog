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
package session2.b_ricart_agrawala

import neko._
import neko.util.MutexClient

/**
  * Distributed mutual exclusion algorithm for complete graphs.
  * Based on "Simple Algorithm Based on Individual Permissions", Sect. 10.2.1, p.249
  * Michel Raynal, "Distributed Algorithms for Message-passing Systems"
  */
class RicartAgrawala(p: ProcessConfig)
  extends ReactiveProtocol(p, "Ricart-Agrawala Mutex")
{
  import RicartAgrawala._
  import ChannelState._

  private var cs_state_i = OUT
  private val R_i = ALL-me
  private var clock_i, lrd_i = 0L
  private var waiting_from_i = Set.empty[PID]
  private var perm_delayed_i = Set.empty[PID]

  def onSend = {
    case MutexClient.Request =>
      cs_state_i = TRYING
      lrd_i = clock_i + 1
      waiting_from_i = R_i
      SEND(Request(me, R_i, lrd_i))
      if (waiting_from_i.isEmpty) enterCS()

    case MutexClient.Release =>
      cs_state_i = OUT
      SEND ( Permission(me, perm_delayed_i) )
      perm_delayed_i = Set.empty
  }

  private def enterCS(): Unit = {
    assert(waiting_from_i.isEmpty)
    cs_state_i = IN
    DELIVER(MutexClient.CanEnter)
  }

  listenTo(classOf[Request])
  listenTo(classOf[Permission])

  def onReceive = {
    case Request(p_j, _, k) =>
      clock_i = math.max(clock_i, k)
      val prio_i = (cs_state_i != OUT) && ( (lrd_i, me) < (k, p_j) )
      if (prio_i) {
        perm_delayed_i += p_j
      }
      else {
        SEND( Permission(me, Set(p_j)) )
      }

    case Permission(p_j, _) =>
      waiting_from_i -= p_j
      if (waiting_from_i.isEmpty) enterCS()
  }
}

object RicartAgrawala
{
  case class Request   (from: PID, to: Set[PID], ts: Long) extends MulticastMessage
  case class Permission(from: PID, to: Set[PID]) extends MulticastMessage

  private object ChannelState extends Enumeration {
    type ChannelState = Value
    val OUT, TRYING, IN = Value
  }
}



