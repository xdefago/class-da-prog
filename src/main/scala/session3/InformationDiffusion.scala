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

class InformationDiffusion(p: ProcessConfig, val myValue : Any)
  extends ActiveProtocol(p, "diffusion")
    with util.DiffusingComputation
    with util.TerminationDetectionClient
{
  import InformationDiffusion._
  import util.TerminationDetectionClient._

  private val initiator = PID(0)
  private var setV_i = Set(Value(me, myValue))
  private var setW_i = Set.empty[Value]

  listenTo(classOf[Gossip])
  listenTo(Terminate.getClass)

  override def run () {
    println(s"${me.name} :> neighbors = " + neighbors.mkString(" "))
    if (me == initiator) initiate()
    while (true) {
      if (setV_i != setW_i) {
        SEND(Gossip(me, neighbors, setV_i diff setW_i))
        setW_i = setV_i
        println(s"${me.name}: V.i = " + setV_i.map(_.v).mkString("{", ",", "}") )
      }
      becomePassive()
      Receive {
        case Gossip(_,_,u) => setV_i = setV_i union u
        case Terminate     => println (s"${me.name}: TERMINATE") ; return
        case _ => /* DROP */
      }
    }
  }
}


object InformationDiffusion
{
  case class Value(p: PID, v: Any)
  case class Gossip(from: PID, to: Set[PID], values: Set[Value])
    extends MulticastMessage
}
