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

import com.joins.kidcenter.controller.LessonIdsResponse
import com.joins.kidcenter.controller.OperationResponse
import com.joins.kidcenter.controller.PlanLessonsResponse
import com.joins.kidcenter.dto.StudentDashboardLesson
import com.joins.kidcenter.dto.internal.ParametrizedMessage
import com.joins.kidcenter.dto.lessons.LessonSlotId
import org.junit.Assert.assertTrue
import kotlin.test.assertEquals

class OperationResponseAssert(private val response: OperationResponse) {
    fun success(): OperationResponseAssert {
        assertTrue(response.success())
        return this
    }

    fun error(text: String, params: Array<Any> = arrayOf()): OperationResponseAssert {
        assertEquals(ParametrizedMessage(text, *params), response.error)
        return this
    }

    fun warning(text: String, params: Array<Any> = arrayOf()): OperationResponseAssert {
        assertEquals(ParametrizedMessage(text, *params), response.warning)
        return this
    }
}

class PlanLessonResponseAssert(private val response: PlanLessonsResponse) {

    fun persisted(persisted: Boolean): PlanLessonResponseAssert {
        assertEquals(persisted, response.persisted)
        return this
    }

    fun plannedLessonSlotIds(vararg plannedLessonSlotIds: LessonSlotId): PlanLessonResponseAssert {
        plannedLessonIds(*plannedLessonSlotIds.map { it.id() }.toTypedArray())
        return this
    }

    fun plannedLessonIds(vararg plannedLessonIds: String): PlanLessonResponseAssert {
        assertTrue(response.plannedLessonIds.keys.containsAll(plannedLessonIds.toList()))
        assertEquals(plannedLessonIds.size, response.plannedLessonIds.size)
        return this
    }

    fun skippedLessons(vararg skippedLessons: String): PlanLessonResponseAssert {
        assertTrue(response.skippedLessons.keys.containsAll(skippedLessons.toList()))
        assertEquals(skippedLessons.size, response.skippedLessons.size)
        return this
    }

    fun studentPlannedLessons(vararg studentPlannedLessons: StudentDashboardLesson): PlanLessonResponseAssert {
        val expectedLessons = response.studentPlannedLessons
        StudentDashboardLessonsAssert(expectedLessons).same(studentPlannedLessons.toList())
        return this
    }
}

class LessonIdsResponseAssert(private val response: LessonIdsResponse) {
    fun success(): LessonIdsResponseAssert {
        assertTrue(response.response.success())
        return this
    }

    fun error(text: String, params: Array<Any> = arrayOf()): LessonIdsResponseAssert {
        assertEquals(ParametrizedMessage(text, *params), response.response.error)
        return this
    }

    fun warning(text: String, params: Array<Any> = arrayOf()): LessonIdsResponseAssert {
        assertEquals(ParametrizedMessage(text, *params), response.response.warning)
        return this
    }

    fun studentPlannedLessons(studentPlannedLessons: List<StudentDashboardLesson>): LessonIdsResponseAssert {
        val expectedLessons = response.studentPlannedLessons
        StudentDashboardLessonsAssert(expectedLessons).same(studentPlannedLessons.toList())
        return this
    }
}

class StudentDashboardLessonsAssert(private val studentPlannedLessons: List<StudentDashboardLesson>) {

    fun same(studentPlannedLessons: List<StudentDashboardLesson>): StudentDashboardLessonsAssert {
        val studentPlannedLessonsList = studentPlannedLessons.toList()
        assertTrue(this.studentPlannedLessons.all { responseLesson ->
            studentPlannedLessonsList.any { plannedLesson -> plannedLesson.equivalent(responseLesson) }
        })
        assertEquals(studentPlannedLessons.size, this.studentPlannedLessons.size)
        return this
    }
}