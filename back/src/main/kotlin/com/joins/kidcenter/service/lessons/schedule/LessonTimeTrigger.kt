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

import com.joins.kidcenter.domain.LessonSubject
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.toDate
import com.joins.kidcenter.utils.toLocalDateTime
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class LessonTimeTrigger(private val duration: Int,
                        private val iteratedLessonTimes: Collection<LessonTime>,
                        private val subjects: Collection<LessonSubject>) : Trigger {

    @Volatile
    var scheduledOperationConext: CloseLessonContext? = null
        private set

    override fun nextExecutionTime(triggerContext: TriggerContext): Date {
        val nextExecutionDateTime = nextExecutionDateTime(triggerContext)
        return nextExecutionDateTime.toDate()!!
    }

    private fun nextExecutionDateTime(triggerContext: TriggerContext): LocalDateTime {
        val lastScheduledDate: Date? = triggerContext.lastScheduledExecutionTime()
        val referenceDateTime: LocalDateTime = lastScheduledDate.toLocalDateTime() ?: DateTimeUtils.currentDateTime()
        val referenceTime: LocalTime = referenceDateTime.toLocalTime()
        val minLessonTimeOption = iteratedLessonTimes.iterator().next()
        val minLessonTime = minLessonTimeOption.endTime(duration)

        iteratedLessonTimes.forEach { lessonTime ->
            val time = lessonTime.endTime(duration)
            if (time.isAfter(referenceTime)) {
                scheduledOperationConext = CloseLessonContext(lessonTime, subjects)
                return referenceDateTime.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
            }
        }
        scheduledOperationConext = CloseLessonContext(minLessonTimeOption, subjects)
        return referenceDateTime.plusDays(1).withHour(minLessonTime.hour).withMinute(minLessonTime.minute)
    }
}

class CloseLessonContext(val lessonTime: LessonTime, val subjects: Collection<LessonSubject>)
