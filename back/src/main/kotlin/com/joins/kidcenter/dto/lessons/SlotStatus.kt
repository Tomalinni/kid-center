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

package com.joins.kidcenter.dto.lessons

import com.joins.kidcenter.domain.LessonCancelType
import com.joins.kidcenter.domain.StudentSlotStatus

class SlotStatus(val lessonId: String,
                 val slotId: Long,
                 val status: StudentSlotStatus,
                 val cancelType: LessonCancelType?)

class SlotStatuses {
    val slotsByLessonId: MutableMap<String, SlotStatus> = mutableMapOf()
    val slotCountByStatus: MutableMap<StudentSlotStatus, Int> = mutableMapOf()
    val slotCountByCancelType: MutableMap<LessonCancelType, Int> = mutableMapOf()

    fun add(slots: Map<String, SlotStatus>): SlotStatuses {
        slotsByLessonId.putAll(slots)
        recalculateCounts()
        return this
    }

    private fun recalculateCounts() {
        slotCountByStatus.clear()
        slotCountByCancelType.clear()
        slotsByLessonId.values.forEach { slot ->
            val countByStatus = slotCountByStatus[slot.status] ?: 0
            slotCountByStatus[slot.status] = countByStatus + 1

            if (slot.cancelType != null) {
                val countByCancelType = slotCountByCancelType[slot.cancelType] ?: 0
                slotCountByCancelType[slot.cancelType] = countByCancelType + 1
            }
        }
    }

    fun lessonsCountByStatus(status: StudentSlotStatus): Int {
        return slotCountByStatus[status] ?: 0
    }

    fun lessonsCountByStatuses(statuses: Collection<StudentSlotStatus>): Int {
        return statuses.map { slotCountByStatus[it] ?: 0 }.sum()
    }

    fun lessonsCountByCancelType(status: LessonCancelType): Int {
        return slotCountByCancelType[status] ?: 0
    }

    fun size() = slotsByLessonId.size

    fun isEmpty() = slotsByLessonId.isEmpty()

    fun studentSlotIds() = slotsByLessonId.values.map { it.slotId }

    fun lessonIdsToSlotIds(): Map<String, Long> {
        return slotsByLessonId.map { e -> Pair(e.key, e.value.slotId) }.toMap()
    }

    fun toList(): List<SlotStatus> = slotsByLessonId.values.toList()
}