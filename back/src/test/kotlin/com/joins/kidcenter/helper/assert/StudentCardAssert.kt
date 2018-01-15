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

import com.joins.kidcenter.domain.StudentCard
import java.time.LocalDate
import kotlin.test.assertEquals

class StudentCardAssert(private val studentCard: StudentCard) {

    fun activated(date: LocalDate): StudentCardAssert {
        assertEquals(date, studentCard.activationDate)
        return this
    }

    fun duration(days: Int): StudentCardAssert {
        assertEquals(days, studentCard.durationDays)
        return this
    }

    fun lessons(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.lessonsLimit)
        assertEquals(available, studentCard.lessonsAvailable)
        return this
    }

    fun cancels(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.cancelsLimit)
        assertEquals(available, studentCard.cancelsAvailable)
        return this
    }

    fun lateCancels(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.lateCancelsLimit)
        assertEquals(available, studentCard.lateCancelsAvailable)
        return this
    }

    fun lastMomentCancels(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.lastMomentCancelsLimit)
        assertEquals(available, studentCard.lastMomentCancelsAvailable)
        return this
    }

    fun undueCancels(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.undueCancelsLimit)
        assertEquals(available, studentCard.undueCancelsAvailable)
        return this
    }

    fun miss(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.missLimit)
        assertEquals(available, studentCard.missAvailable)
        return this
    }

    fun suspends(total: Int, available: Int): StudentCardAssert {
        assertEquals(total, studentCard.suspendsLimit)
        assertEquals(available, studentCard.suspendsAvailable)
        return this
    }
}