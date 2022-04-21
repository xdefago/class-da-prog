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
package session2.a_centralized

import neko._
import neko.util.MutexClient

/**
  * Centralized (home-based) algorithm for distributed mutual exclusion.
  * Based on "Three-Way Handshake Algorithm", Sect. 5.1.3, p.94
  * Michel Raynal, "Distributed Algorithms for Message-passing Systems"
  */
object HomeBasedMutex
{
  /*
   * Client part
   */
  class Client(p: ProcessConfig, home: PID)
    extends ReactiveProtocol(p, "mutex: client")
  {
    def onSend = {
      case MutexClient.Request => SEND(Request(me, home))
      case MutexClient.Release => SEND(Release(me, home))
    }

    listenTo(classOf[PrivObject])

    def onReceive = {
      case _ : PrivObject => DELIVER(MutexClient.CanEnter)
    }
  }

  /*
   * Server part
   */
  class Server(p: ProcessConfig)
    extends ReactiveProtocol(p, "mutex: home")
  {
    private var object_present_i = true
    private var queue_i = Seq.empty[Request]

    listenTo(classOf[Request])
    listenTo(classOf[Release])

    def onReceive = {
      case Request(from, _) if object_present_i =>
        object_present_i = false
        SEND(PrivObject(me, from))
      case r: Request if !object_present_i => queue_i = queue_i :+ r
      case _: Release if queue_i.isEmpty   => object_present_i = true
      case _: Release =>
        queue_i.headOption.foreach { nextRequest =>
          queue_i = queue_i.tail
          SEND(PrivObject(me, nextRequest.from))
        }
    }
    def onSend = PartialFunction.empty[Event, Unit]
  }

  /*
   * Message types
   */
  case class Request(from: PID, to: PID) extends UnicastMessage
  case class PrivObject(from: PID, to: PID) extends UnicastMessage
  case class Release(from: PID, to: PID) extends UnicastMessage
}
