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

package prog3

import neko.PID
import neko.topology.Topology

object SpanningTreeHelper
{
  /**
    * Given a connected topology and a process p in that topology, construct a BFS spanning tree rooted at p.
    * The spanning tree is expressed as a mapping from a process identity to its parent. The root has itself as parent.
    *
    * @param root the identity of the root of the spanning tree
    * @param topo the topology that the tree must span
    * @return the spanning expressed as a mapping from a process to its parent.
    */
  def spanningTree(root: PID, topo: Topology): Map[PID, PID] = {
    assert(topo.isConnected)
    assert(topo.contains(root))

    var parentOf = Map(root -> root)
    var assigned = Set(root)

    var frontline = Set(root)

    while(frontline.nonEmpty) {
      var nextLine = Set.empty[PID]
      for {
        root <- frontline
        child <- topo.neighborsFor(root).getOrElse(Set.empty)
        if !assigned.contains(child)
      } {
        assigned += child
        parentOf += child -> root
        nextLine += child
      }
      frontline = nextLine
    }

    parentOf
  }
}
