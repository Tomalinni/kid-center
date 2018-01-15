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

package com.joins.kidcenter.service.sms.scheduling

import com.joins.kidcenter.Config
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.toDate
import com.joins.kidcenter.utils.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class LessonTimeTrigger : Trigger {
    private val log = LoggerFactory.getLogger(LessonTimeTrigger::class.java)!!

    override fun nextExecutionTime(triggerContext: TriggerContext?): Date? {
        val executionTime = findNextExecutionTime(triggerContext)
        log.info("Next hour-before notification will be sent at $executionTime")
        return executionTime?.toDate()
    }

    private fun findNextExecutionTime(triggerContext: TriggerContext?): LocalDateTime? {
        if (triggerContext != null) {
            val lastScheduledExecutionTime: LocalTime? = triggerContext.lastScheduledExecutionTime().toLocalDateTime()?.toLocalTime()
            val lastProcessedLessonTime = if (lastScheduledExecutionTime == null) null else DateTimeUtils.nextLessonTime(lastScheduledExecutionTime)

            val currentDateTime = DateTimeUtils.currentDateTime()
            if (lastProcessedLessonTime == null) { //first trigger call, use next lesson time
                return findNextNotificationTime(currentDateTime)
            } else {
                val lastProcessedLessonLocalDateTime = DateTimeUtils.withLessonTime(currentDateTime.toLocalDate(), lastProcessedLessonTime)
                val nextLessonLocalDateTime = findNextNotificationTime(lastProcessedLessonLocalDateTime)
                if (nextLessonLocalDateTime.isBefore(currentDateTime)) { //sending of notification froze for some reason for several hours, find next lesson time from current moment
                    return findNextNotificationTime(currentDateTime)
                }
                return nextLessonLocalDateTime
            }
        }
        return null //should not happen
    }

    private fun findNextNotificationTime(dateTime: LocalDateTime) =
            DateTimeUtils.nextLessonLocalDateTime(dateTime.plusMinutes(Config.currentDayLessonsNotificationGapMins)).minusMinutes(Config.currentDayLessonsNotificationGapMins)
}