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

package session2

import neko._
import neko.util.MutexClient

/**
 * Created by defago on 2017/05/10.
 */
class TrivialNoMutex(p: ProcessConfig)
  extends ReactiveProtocol(p, "Trivial No Mutex")
{
  def onSend = {
    case MutexClient.Request => DELIVER(MutexClient.CanEnter)
    case MutexClient.Release =>
  }

  def onReceive = PartialFunction.empty
}


object TrivialNoMutexMain extends Main(topology.Clique(10))(
  ProcessInitializer { p =>
    val app   = new MutexApplication(p)
    val nomutex = new TrivialNoMutex(p)
    app --> nomutex
  })
