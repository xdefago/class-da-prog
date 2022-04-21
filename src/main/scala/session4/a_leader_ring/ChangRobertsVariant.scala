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
package session4.a_leader_ring

import neko._

class ChangRobertsVariant(c: ProcessConfig) extends ReactiveProtocol(c, "election")
{
  import ChangRobertsVariant._
  import neko.util.LeaderElectionClient._

  private val next  = me.map (i => (i+1) % N )
  private var idmax = me

  def onSend = {
    case Candidate =>
      if (idmax == me) SEND (Election(me, next, me))
  }

  listenTo(classOf[Election])
  listenTo(classOf[AnnounceLeader])

  def onReceive = {
    case Election(_,_,candidate) if candidate > idmax =>
      idmax = candidate
      SEND( Election(me, next, candidate) )

    case Election(_,_,candidate) if candidate < me => /* skip */

    case Election(_,_,`me`) => SEND( AnnounceLeader(me,ALL,me) )

    case AnnounceLeader(_,_,leader) => DELIVER( Elected(Some(leader)) )

    case _ => /* otherwise: IGNORE */
  }
}


object ChangRobertsVariant
{
  case class Election(from: PID, to: PID, candidate: PID) extends UnicastMessage
  case class AnnounceLeader(from: PID, to: Set[PID], leader: PID) extends MulticastMessage
}
