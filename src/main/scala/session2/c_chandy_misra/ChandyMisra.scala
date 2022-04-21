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

package session2.c_chandy_misra

import neko._
import neko.util.MutexClient

/**
  * Distributed mutual exclusion algorithm for complete graphs.
  * Based on "Simple Algorithm Based on Individual Permissions", Sect. 10.2.1, p.249
  * Michel Raynal, "Distributed Algorithms for Message-passing Systems"
  */
class ChandyMisra(p: ProcessConfig)
  extends ReactiveProtocol(p, "Chandy-Misra Mutex")
{
  import ChandyMisra._
  import ChannelState._
  import PermissionState._

  private var cs_state_i: ChannelState = OUT
  private var perm_delayed_i = Set.empty[PID]
  private var perm_state_i = ALL.map(_ -> USED).toMap
  private var R_i = ALL.filter(me < _)    // includes all processes greater than me


  def onSend = {
    /* operation acquire_mutex() */
    case MutexClient.Request =>
      // TODO: implement operation acquire_mutex()
      DELIVER(MutexClient.CanEnter) // <-- wrong place! TODO: remove

    /* operation release_mutex() */
    case MutexClient.Release =>
      // TODO: implement operation release_mutex()
  }

  private def enterCS() {
    assert(R_i.isEmpty)
    cs_state_i = IN
    perm_state_i = perm_state_i.mapValues(_ => USED).toMap
    DELIVER(MutexClient.CanEnter)
  }

  listenTo(classOf[Request])
  listenTo(classOf[Permission])

  def onReceive = {
    case Request(p_j, _) =>
      // TODO: implement

    case Permission(j, i) =>
      // TODO: implement

    case _ => assert(false)
  }
}

object ChandyMisra
{
  case class Request (from: PID, to: Set[PID]) extends MulticastMessage
  case class Permission(from: PID, to: PID)    extends UnicastMessage

  private object ChannelState extends Enumeration {
    type ChannelState = Value
    val OUT, TRYING, IN = Value
  }
  private object PermissionState extends Enumeration {
    type PermissionState = Value
    val NEW, USED = Value
  }
}
