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

import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentStatus
import com.joins.kidcenter.dto.lessons.LessonSlotIds
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StudentAssert(private val student: Student) {

    fun status(status: StudentStatus): StudentAssert {
        assertEquals(status, student.status)
        return this
    }

    fun businessId(businessId: String): StudentAssert {
        assertEquals(businessId, student.businessId)
        return this
    }

    fun trialBusinessId(businessId: String): StudentAssert {
        assertEquals(businessId, student.trialBusinessId)
        return this
    }

    fun paidLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.paidLessonsCount)
        return this
    }

    fun bonusLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.bonusLessonsCount)
        return this
    }

    fun plannedLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.plannedLessonsCount)
        return this
    }

    fun visitedLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.visitedLessonsCount)
        return this
    }

    fun usedLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.usedLessonsCount)
        return this
    }

    fun availableLessonsCount(count: Int): StudentAssert {
        assertEquals(count, student.availableLessonsCount)
        return this
    }

    fun lastCardValidDate(date: LocalDate?): StudentAssert {
        assertEquals(date, student.lastCardValidDate)
        return this
    }

    fun nextLessons(lessons: List<String>): StudentAssert {
        return nextLessons(LessonSlotIds.fromString(lessons))
    }

    fun nextLessons(lessons: LessonSlotIds): StudentAssert {
        assertEquals(lessons, LessonSlotIds.fromString(student.nextLessons))
        return this
    }

    fun relativeIds(ids: Collection<Long>): StudentAssert {
        val savedStudentRelativeIds = student.relatives.filter { it.id != null }.map { it.id!! }
        assertTrue(ids.containsAll(savedStudentRelativeIds), "Expected $ids but was $savedStudentRelativeIds")
        assertEquals(ids.size, student.relatives.size)
        return this
    }

    fun siblingIds(ids: Collection<Long>): StudentAssert {
        val savedStudentSiblingIds = student.siblings.filter { it.id != null }.map { it.id!! }
        assertTrue(ids.containsAll(savedStudentSiblingIds), "Expected $ids but was $savedStudentSiblingIds")
        assertEquals(ids.size, student.siblings.size)
        return this
    }

    fun familyId(id: Long): StudentAssert {
        assertNotNull(student.family)
        assertEquals(id, student.family!!.id)
        return this
    }

    fun noFamily(): StudentAssert {
        assertNull(student.family)
        return this
    }
}
