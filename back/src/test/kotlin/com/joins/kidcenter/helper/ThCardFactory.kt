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
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

interface ThCardFactory {
    fun regular(): Card
    fun trial(): Card
    fun bonus(): Card
}

@Component
class ThCardFactoryImpl : ThCardFactory {

    val counter = AtomicInteger()

    override fun regular(): Card {
        return Card().apply {
            durationDays = 365
            durationDaysMax = 365
            lessonsLimit = 20
            cancelsLimit = 5
            lateCancelsLimit = 1
            lastMomentCancelsLimit = 1
            undueCancelsLimit = 1
            missLimit = 1
            suspendsLimit = 5
        }
    }

    override fun trial(): Card {
        return Card().apply {
            durationDays = 14
            durationDaysMax = 14
            lessonsLimit = 2
            cancelsLimit = 1
            lateCancelsLimit = 1
            lastMomentCancelsLimit = 1
            undueCancelsLimit = 1
            missLimit = 1
            suspendsLimit = 1
        }

    }

    override fun bonus(): Card {
        return Card().apply {
            durationDays = 30
            durationDaysMax = 30
            lessonsLimit = 4
            cancelsLimit = 1
            lateCancelsLimit = 1
            lastMomentCancelsLimit = 1
            undueCancelsLimit = 1
            missLimit = 1
            suspendsLimit = 1
        }
    }
}