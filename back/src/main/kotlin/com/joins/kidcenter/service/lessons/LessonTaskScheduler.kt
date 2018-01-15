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

package com.joins.kidcenter.service.lessons

import com.joins.kidcenter.domain.LessonSubject.*
import com.joins.kidcenter.service.lessons.schedule.LessonTime.*
import com.joins.kidcenter.service.lessons.schedule.LessonTimeTrigger
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.LoggingRunnableDecorator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component

@Component
open class LessonTaskScheduler @Autowired constructor(
        val lessonSlotService: LessonSlotService,
        @Qualifier("closeLessonScheduler")
        private val closeLessonScheduler: TaskScheduler
) {
    val log: Logger = LoggerFactory.getLogger(LessonSlotService::class.java)

    private val age1_2LessonTimeTrigger = LessonTimeTrigger(30,
            listOf(t0900, t0945, t1030, t1115, t1200),
            listOf(z_sFitness, y_sMusic, x_sArt))
    private val age2_7LessonTimeTrigger = LessonTimeTrigger(60,
            listOf(t0900, t1015, t1130, t1330, t1445, t1600, t1715, t1830),
            listOf(fitness, cooking, art, english, lego, fitness, ballet))

    init {
        val closeLessonsTask = LoggingRunnableDecorator("Close finished lesson", Runnable {
            val conext = age2_7LessonTimeTrigger.scheduledOperationConext!!
            val lessonStartDateTime = conext.lessonTime.startLocalDateTime(DateTimeUtils.currentDate())
            lessonSlotService.closeLessonsByDateTimeAndSubjects(lessonStartDateTime, conext.subjects)
        })
        closeLessonScheduler.schedule(closeLessonsTask, age1_2LessonTimeTrigger)
        closeLessonScheduler.schedule(closeLessonsTask, age2_7LessonTimeTrigger)
    }
}
