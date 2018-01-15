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

package com.joins.kidcenter.helper

import com.joins.kidcenter.domain.Card
import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentCard
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

interface ThStudentCardFactory {
    fun fromStudentAndCard(student: Student, card: Card): StudentCard
}

@Component
class ThStudentCardFactoryImpl : ThStudentCardFactory {

    val counter = AtomicInteger()

    override fun fromStudentAndCard(student: Student, card: Card): StudentCard {
        return StudentCard().apply {
            this.student = student
            cardId = card.id
            price = 100
            visitType = card.visitType
            durationDays = card.durationDays
            lessonsLimit = card.lessonsLimit
            lessonsAvailable = card.lessonsLimit
            cancelsLimit = card.cancelsLimit
            cancelsAvailable = card.cancelsLimit
            lateCancelsLimit = card.lateCancelsLimit
            lateCancelsAvailable = card.lateCancelsLimit
            lastMomentCancelsLimit = card.lastMomentCancelsLimit
            lastMomentCancelsAvailable = card.lastMomentCancelsLimit
            undueCancelsLimit = card.undueCancelsLimit
            undueCancelsAvailable = card.undueCancelsLimit
            missLimit = card.missLimit
            missAvailable = card.missLimit
            suspendsLimit = card.suspendsLimit
            suspendsAvailable = card.suspendsLimit
        }
    }
}