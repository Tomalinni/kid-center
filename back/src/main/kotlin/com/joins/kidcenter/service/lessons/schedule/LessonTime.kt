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

package com.joins.kidcenter.service.lessons.schedule

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class LessonTime(hours: Int, minutes: Int) {
    t0900(9, 0),
    t0945(9, 45),
    t1015(10, 15),
    t1030(10, 30),
    t1115(11, 15),
    t1130(11, 30),
    t1200(12, 0),
    t1330(13, 30),
    t1445(14, 45),
    t1600(16, 0),
    t1715(17, 15),
    t1830(18, 30);

    val startTime: LocalTime = LocalTime.of(hours, minutes)
    fun endTime(duration: Int): LocalTime = startTime.plusMinutes(duration.toLong())

    fun startLocalDateTime(date: LocalDate): LocalDateTime = LocalDateTime.of(date, startTime)
}
