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

package com.joins.kidcenter.dto

import com.joins.kidcenter.Config
import java.time.LocalDate

data class DateRange(val startDate: LocalDate, val endDate: LocalDate) {

    fun withStartDate(startDate: LocalDate) = DateRange(startDate, this.endDate)

    fun withEndDate(endDate: LocalDate) = DateRange(this.startDate, endDate)

    fun withMinStartDate(startDate: LocalDate) =
            if (startDate.isBefore(this.startDate)) withStartDate(startDate) else this

    fun withMaxEndDate(endDate: LocalDate) =
            if (endDate.isAfter(this.endDate)) withEndDate(endDate) else this

    companion object {
        val eternity = DateRange(Config.minDate, Config.maxDate)

        fun singleDate(date: LocalDate) = DateRange(date, date)
    }
}

