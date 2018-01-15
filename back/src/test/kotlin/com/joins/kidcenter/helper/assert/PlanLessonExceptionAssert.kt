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

import com.joins.kidcenter.controller.PlanLessonsResponse
import com.joins.kidcenter.service.exceptions.OperationException
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import kotlin.test.assertEquals

class PlanLessonExceptionAssert(e: Throwable) {
    val exception: OperationException
    val response: PlanLessonsResponse

    init {
        assertTrue("Unexpected exception class. Actual class is ${e.javaClass}", e is OperationException)
        exception = e as OperationException
        response = exception.result as PlanLessonsResponse
    }

    fun skippedLessons(skippedLessons: Collection<String>): PlanLessonExceptionAssert {
        assertTrue(response.skippedLessons.keys.containsAll(skippedLessons))
        return this
    }

    fun text(text: String): PlanLessonExceptionAssert {
        assertNotNull(response.response.error)
        assertEquals(text, response.response.error!!.text)
        return this
    }
}