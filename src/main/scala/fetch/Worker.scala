/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fetch

import java.util.concurrent.atomic.AtomicInteger

object ExecutionState {
  // using ints here because of AtomicInteger simplicity
  final val IDLE = 0
  final val BUSY = 1
  final val STOPPING = 2
  final val TERMINATED = 3
}


trait Continuer {
  def isBusy: Boolean
}


case class TaskInfo(id: Option[Int], description: String)


case class Task(info: TaskInfo, action: (Continuer) => Unit)


object Task {
  private val idGenerator = new AtomicInteger()

  def apply(description: String, action: (Continuer) => Unit): Task = {
    new Task(TaskInfo(Some(idGenerator.incrementAndGet), description), action)
  }
}
