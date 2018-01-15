/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joins.kidcenter.service.lessons

interface LessonEventListener {
    fun onLessonEvent(event: LessonEvent)
}

class LessonEvent(val studentCardId: Long,
                  val studentSlotIds: Set<Long>,
                  val lessonSlotIds: Set<String>,
                  val type: LessonEventType)

enum class LessonEventType(val label: String) {
    plan("预约"), unplan("取消"), cancel("取消"), visit("计划"), miss("逃课"), revoke("请假")
}

class LessonEventListeners constructor(
        val listeners: List<LessonEventListener>
) : LessonEventListener {

    override fun onLessonEvent(event: LessonEvent) {
        listeners.forEach { it.onLessonEvent(event) }
    }
}
