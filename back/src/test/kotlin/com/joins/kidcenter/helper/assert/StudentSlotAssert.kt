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

package com.joins.kidcenter.helper.assert

import com.joins.kidcenter.domain.LessonCancelType
import com.joins.kidcenter.domain.StudentSlot
import com.joins.kidcenter.domain.StudentSlotStatus
import com.joins.kidcenter.domain.VisitType
import com.joins.kidcenter.dto.lessons.LessonSlotId
import kotlin.test.assertEquals

class StudentSlotAssert(private val studentSlot: StudentSlot) {

    fun lesson(id: LessonSlotId): StudentSlotAssert {
        assertEquals(id, LessonSlotId.fromString(studentSlot.lesson!!.id))
        return this
    }

    fun student(id: Long): StudentSlotAssert {
        assertEquals(id, studentSlot.student!!.id!!)
        return this
    }

    fun card(id: Long): StudentSlotAssert {
        assertEquals(id, studentSlot.card!!.id!!)
        return this
    }

    fun visitType(visitYpe: VisitType): StudentSlotAssert {
        assertEquals(visitYpe, studentSlot.visitType)
        return this
    }

    fun status(status: StudentSlotStatus): StudentSlotAssert {
        assertEquals(status, studentSlot.status)
        return this
    }

    fun repeatsLeft(count: Int): StudentSlotAssert {
        assertEquals(count, studentSlot.repeatsLeft)
        return this
    }

    fun cancelType(cancelType: LessonCancelType?): StudentSlotAssert {
        assertEquals(cancelType, studentSlot.cancelType)
        return this
    }

    fun invalidated(invalidated: Boolean): StudentSlotAssert {
        assertEquals(invalidated, studentSlot.invalidated)
        return this
    }
}
