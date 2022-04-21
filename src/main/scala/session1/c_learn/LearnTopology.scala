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

package session1.c_learn

import neko._


/*
 * Book: Distributed Algorithms for Message-Passing Systems
 * Algorithm: Learning the communication graph.
 * Pseudocode in Figure 1.3
 */
class LearnTopology(p: ProcessConfig)
  extends ReactiveProtocol(p, "Learn")
{
  private var part_i           = false
  private var proc_known_i     = Set(me)
  private var channels_known_i = neighbors.map(id_j => Set(me, id_j))

  import LearnTopology._

  /*
   * operation start() is...
   */
  private def learnStart() : Unit = {
    for (id_j <- neighbors) {
      SEND(PositionMsg(me, id_j, Position(me, neighbors)))
    }
    part_i = true
  }


  listenTo(LearnTopologyClient.Start.getClass)
  def onSend = {
    /*
     * when START() is received do...
     */
    case LearnTopologyClient.Start =>
      if (! part_i) learnStart()
  }


  listenTo(classOf[PositionMsg])
  def onReceive = {
    /*
     * when POSITION(id, neighbors) is received from neighbor identified id_x do...
     */
    case PositionMsg(id_x, _, position) =>
      if (! part_i) learnStart()
      if ( ! proc_known_i.contains(position.id)) {
        proc_known_i += position.id
        for (id_k <- position.neighbors) {
          channels_known_i += Set(position.id, id_k)
        }
        for (id_y <- neighbors - id_x) {
          SEND(PositionMsg(me, id_y, position))
        }
        if (channels_known_i.forall(c => c.forall(proc => proc_known_i.contains(proc)))) {
          val edges =
            channels_known_i
              .filter(c => c.size == 2)
              .map(c => (c.min, c.max))
          DELIVER(LearnTopologyClient.Learn(edges))
        }
      }
  }
}

object LearnTopology
{
  case class Position(id: PID, neighbors: Set[PID])

  case class PositionMsg(from: PID, to: PID, pos: Position)
    extends UnicastMessage
}
