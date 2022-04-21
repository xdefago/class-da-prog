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

package prog1

import neko._

class BroadcastConvergecast(p: ProcessConfig)
  extends ReactiveProtocol(p, "PIF")
{
  import BroadcastConvergecast._

  assert (! neighbors.contains(me))

  private var parent   = Option.empty[PID]
  private var children = Set.empty[PID]
  private var expected_msg = 0
  private var val_set  = Set.empty[(PID,ValueType)]

  private def myValue: ValueType = children


  listenTo(classOf[START])

  def onSend = {
    case START(data) =>
      println(s"${me.name} initiating PIF protocol")
      // TODO: implement
  }

  listenTo(classOf[GO])
  listenTo(classOf[BACK])

  def onReceive = {

    /*
     * TODO: implement the protocol here
     */

    case unknown =>
      System.err.println(s"OOPS! ${me.name} received unknown message $unknown")
  }
}

object BroadcastConvergecast
{
  type ValueType = Set[PID]
  type DataType  = String

  case class START(data: DataType) extends Signal
  case class VISIT(data: DataType) extends Signal
  case class TERMINATE(values: Set[(PID, ValueType)]) extends Signal

  case class GO(from: PID, to: Set[PID], data: DataType)
    extends MulticastMessage

  case class BACK(from: PID, to: PID, values:Set[(PID,ValueType)])
    extends UnicastMessage
}
