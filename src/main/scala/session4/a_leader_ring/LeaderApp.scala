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


class LeaderApp(p: ProcessConfig)
  extends ActiveProtocol(p, "leader app")
    with neko.util.LeaderElectionClient
{
  import neko.util.LeaderElectionClient._

  def run(): Unit = {
    candidate()
    Receive {
      case Elected(Some(leader)) if leader == me =>
        println(s"${me.name} :> I AM THE LEADER (${me.name}) !!!!")

      case Elected(Some(leader)) =>
        println(s"${me.name} :> the leader is ${leader.name}")

      case Elected(None) =>
        println(s"${me.name} :> I'm not a leader")

      case _ => ??? /* unknown message */
    }
  }
}
