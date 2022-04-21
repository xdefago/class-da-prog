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
package session3

import neko._


class Safra(p: ProcessConfig)
  extends ReactiveProtocol(p, "Safra")
{
  import Safra._
  import util.TerminationDetectionClient._

  private val initiator = PID(0)
  private val nextProcess = me.map(i => (i + N - 1) % N)
  private var msgCount = 0
  private var state : State         = Active
  private var color : StateColor    = White
  private var token : Option[Token] = if (me == initiator) Some(Token(White, 0)) else None


  private def transmitToken(tok: Option[Token]) {
    tok match {
      case Some(Token(col, q)) =>
        val tokenToTransmit =
          if (me == initiator)     Token(White, 0)             // Rule 1 + 6
          else if (color == White) Token(col,   msgCount + q)  // Rule 2
          else                     Token(Black, msgCount + q)  // Rule 2 + 4
        color = White // Rule 6 + 7
        SEND(TokenCarrier(me, nextProcess, tokenToTransmit))
      case _ =>
    }
    this.token = None
  }

  def onSend = {
    case util.DiffusingComputation.Initiate =>
      /* ignore (compat. w/diffusing computations) */

    case BecomePassive if state == Active =>
      state = Passive
      transmitToken(token)   // Rule 2

    case BecomePassive if state == Passive =>
      throw new Exception(s"A process (${me.name}) cannot turn passive when it is already passive.")

    case m : Message if state == Active =>
      msgCount += m.destinations.size // Rule 0
      SEND(Payload(m))

    case m if state == Passive =>
      throw new Exception(s"A process (${me.name}) cannot send messages while passive: $m")

  }

  listenTo (classOf[Announce])
  listenTo (classOf[Payload])
  listenTo (classOf[TokenCarrier])

  def onReceive = {
    case _ : Announce => DELIVER (Terminate)
    case Payload(m) =>
      color = Black    // Rule 3
      state = Active
      msgCount -= 1    // Rule 0
      DELIVER (m)

    case TokenCarrier(_,_,Token(White,q))
      if me == initiator && (state == Passive) && (color == White) && (msgCount + q == 0) =>
      SEND(Announce(me,ALL))

    case TokenCarrier(_,_,tok) if state == Active  => token = Some(tok)         // Rule 2
    case TokenCarrier(_,_,tok) if state == Passive => transmitToken(Some(tok))  // Rule 2/5
  }
}


object Safra
{
  sealed abstract class StateColor
  case object Black extends StateColor
  case object White extends StateColor

  sealed abstract class State
  case object Active  extends State
  case object Passive extends State

  case class Payload(m: Message) extends Wrapper(m)
  case class Announce(from: PID, to: Set[PID]) extends MulticastMessage

  case class Token (color: StateColor, count: Long)
  case class TokenCarrier(from: PID, to: PID, token: Token)
    extends UnicastMessage
}
