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

class ChangRoberts(c: ProcessConfig) extends ReactiveProtocol(c, "election")
{
  import ChangRoberts._
  import neko.util.LeaderElectionClient._

  private val next = me.map (i => (i+1) % N )
  private var part_i    = false
  private var elected_i = false
  private var done_i    = false
  private var leader_i  = Option.empty[PID]

  def onSend = {
    case Candidate =>
      if (! part_i) {
        part_i = true
        SEND (Election(me, next, me))
      }
  }

  listenTo(classOf[Election])
  listenTo(classOf[AnnounceLeader])

  def onReceive = {
    case Election(_,_,id) if id > me =>
      part_i = true
      SEND( Election(me, next, id) )

    case Election(_,_,id) if id < me =>
      if (! part_i) {
        part_i = true
        SEND( Election(me, next, id) )
      }

    case Election(_,_,id) if id == me=>
      elected_i = true
      SEND( AnnounceLeader(me, ALL, id) )

    case AnnounceLeader(_,_,id) =>
      leader_i = Some(id)
      done_i = true
      DELIVER( Elected(Some(id)) )

    case _ => /* otherwise: IGNORE */
  }
}


object ChangRoberts
{
  case class Election(from: PID, to: PID, candidate: PID) extends UnicastMessage
  case class AnnounceLeader(from: PID, to: Set[PID], leader: PID) extends MulticastMessage

  sealed abstract class StateColor
  case object Red   extends StateColor
  case object Black extends StateColor
}
