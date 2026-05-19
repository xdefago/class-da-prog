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
package session4.b_leader_anonymous

import neko._


/**
 * Randomized leader election in anonymous networks in constant expected number of rounds.
 *
 * @param p process configuration
 */
class AnonLeader(p: ProcessConfig) extends ReactiveProtocol(p, "anonymous leader")
{
  import AnonLeaderNaive._
  import protocol.FastAnonymousRounds._

  def generateRandomBit(candidates: Int): Boolean = {
    // rand.nextBoolean()
    rand.nextDouble() * candidates < 1.0
  }

  val rand  = new scala.util.Random()
  var myBit = generateRandomBit(N)


  listenTo(InitRound.getClass)
  listenTo(classOf[StartRound])

  def onReceive =
  {
    case InitRound =>
      SEND(Draw(myBit))

    case StartRound(round, info) =>
      val roundData = info.map(_.asInstanceOf[Draw])

      displayRound(round, roundData)

      val countTrue = roundData.count(_.bit == true)

      if (countTrue == 1) {
        if (myBit) {
          /* LEADER */
          println (s"${me.name } LEADER !!!!! (round = $round)")
        }
        SEND(Done)
      }
      else {
        if (myBit || countTrue == 0) {
          val candidates = if (countTrue > 0) countTrue else N
          myBit = generateRandomBit(candidates)
        }
        SEND(Draw(myBit))
      }
  }

  def onSend = PartialFunction.empty[neko.Event,Unit]


  private def displayRound(round: Int, roundData: Seq[Draw]): Unit =
    if (me == PID(0)) { // Display information on the round
      val vect   = roundData.map(d => if (d.bit) "O" else "_").mkString(" ")
      println(s"$round : $vect")
    }
}


object AnonLeader
{
  case class Draw(bit: Boolean) extends protocol.FastAnonymousRounds.Anonymous
}


object AnonLeaderMain
  extends Main(topology.Clique(1000))(
    ProcessInitializer { p=>
      val leader = new AnonLeader(p)
      val anon   = new protocol.FastAnonymousRounds(p)
      leader --> anon
    }
  )
