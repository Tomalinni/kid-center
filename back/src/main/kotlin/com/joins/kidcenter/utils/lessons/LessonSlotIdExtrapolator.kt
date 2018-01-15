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

package com.joins.kidcenter.utils.lessons

import com.joins.kidcenter.dto.lessons.LessonSlotId
import java.time.LocalDate

class LessonSlotIdExtrapolator(val ids: List<LessonSlotId>,
                               val endDate: LocalDate) {
    var weeksShift: Long = 1
    var lessonIdIndex = 0

    fun next(lessonIdCount: Int): List<LessonSlotId> {
        val ids = mutableListOf<LessonSlotId>()
        while (ids.size < lessonIdCount) {
            val nextLessonId = next() ?: break
            ids.add(nextLessonId)
        }
        return ids
    }

    private fun next(): LessonSlotId? {
        if (ids.isEmpty()) {
            return null
        }
        val lessonSlotId = ids[lessonIdIndex]
        val nextDateTime = lessonSlotId.dateTime.plusWeeks(weeksShift)
        val nextLessonId = if (!nextDateTime.toLocalDate().isAfter(endDate)) lessonSlotId.withDateTime(nextDateTime) else null
        lessonIdIndex++
        if (lessonIdIndex >= ids.size) {
            lessonIdIndex = 0
            weeksShift++
        }
        return nextLessonId
    }
}