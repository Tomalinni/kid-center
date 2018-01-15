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

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.StudentDashboardLesson
import com.joins.kidcenter.dto.lessons.LessonSlotId
import com.joins.kidcenter.dto.lessons.LessonTemplateSaveDto
import com.joins.kidcenter.utils.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

interface ThLessonFactory {
    fun lessonTemplate(): LessonTemplateBuilder

    fun lessonSlotByDateTime(dateTime: LocalDateTime, subject: LessonSubject): LessonSlotId

    fun lessonSlotInPast(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId

    fun lessonSlotInPastOrToday(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId

    fun lessonSlotInPast(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject): LessonSlotId

    fun lessonSlotInFuture(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId

    fun lessonSlotInFutureOrToday(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId

    fun lessonSlotInFuture(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject): LessonSlotId

    fun repeatedLessonSlots(firstLesson: LessonSlotId, repeatsCount: Int): List<LessonSlotId>

    fun studentDashboardLesson(lesson: LessonSlotId, visitType: VisitType, ageGroup: LessonAgeGroup, status: StudentSlotStatus, cancelType: LessonCancelType?, invalidated: Boolean, repeatsLeft: Int = 1, modifiedDate: LocalDateTime = DateTimeUtils.currentDateTime(), modifiedBy: String = "system"): StudentDashboardLesson

    fun repeatedStudentDashboardLesson(lesson: LessonSlotId, visitType: VisitType, ageGroup: LessonAgeGroup, status: StudentSlotStatus, cancelType: LessonCancelType?, invalidated: Boolean, repeatsCount: Int): List<StudentDashboardLesson>
}

@Component
class ThLessonFactoryImpl : ThLessonFactory {
    val counter = AtomicInteger()

    override fun lessonTemplate(): LessonTemplateBuilder {
        val templateIndex = counter.incrementAndGet()
        val lessonTemplate = LessonTemplate().apply {
            name = "Template$templateIndex"
        }
        return LessonTemplateBuilder(lessonTemplate)
    }

    override fun lessonSlotByDateTime(dateTime: LocalDateTime, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(dateTime, subject)
    }

    override fun lessonSlotInPast(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().withHour(hours).withMinute(minutes).withDayOfWeekInPast(day.toWeekDay()), subject)
    }

    override fun lessonSlotInPastOrToday(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().withHour(hours).withMinute(minutes).withDayOfWeekInPastOrToday(day.toWeekDay()), subject)
    }

    override fun lessonSlotInPast(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().minusDays(daysDiff).minusHours(hoursDiff).minusMinutes(minutesDiff), subject)
    }

    override fun lessonSlotInFuture(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().withHour(hours).withMinute(minutes).withDayOfWeekInFuture(day.toWeekDay()), subject)
    }

    override fun lessonSlotInFutureOrToday(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().withHour(hours).withMinute(minutes).withDayOfWeekInFutureOrToday(day.toWeekDay()), subject)
    }

    override fun lessonSlotInFuture(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject): LessonSlotId {
        return LessonSlotId(DateTimeUtils.currentDateTime().plusDays(daysDiff).plusHours(hoursDiff).plusMinutes(minutesDiff), subject)
    }

    override fun repeatedLessonSlots(firstLesson: LessonSlotId, repeatsCount: Int): List<LessonSlotId> {
        val repeatedLessons: MutableList<LessonSlotId> = mutableListOf(firstLesson)
        if (repeatsCount > 1) {
            @Suppress("LoopToCallChain")
            for (i in 1..repeatsCount - 1) {
                val repeatedLesson = firstLesson.dateTime.plusWeeks(i.toLong())
                repeatedLessons.add(firstLesson.withDateTime(repeatedLesson))
            }
        }
        return repeatedLessons
    }

    override fun studentDashboardLesson(lesson: LessonSlotId, visitType: VisitType, ageGroup: LessonAgeGroup, status: StudentSlotStatus, cancelType: LessonCancelType?, invalidated: Boolean, repeatsLeft: Int, modifiedDate: LocalDateTime, modifiedBy: String): StudentDashboardLesson {
        return StudentDashboardLesson(0L, visitType, repeatsLeft, status, cancelType, invalidated, lesson.id(), ageGroup, modifiedDate, modifiedBy)
    }

    override fun repeatedStudentDashboardLesson(lesson: LessonSlotId, visitType: VisitType, ageGroup: LessonAgeGroup, status: StudentSlotStatus, cancelType: LessonCancelType?, invalidated: Boolean, repeatsCount: Int): List<StudentDashboardLesson> {
        val firstDashboardLesson = studentDashboardLesson(lesson, visitType, ageGroup, status, cancelType, invalidated, repeatsCount)
        val firstLesson = LessonSlotId.fromString(firstDashboardLesson.lessonId)
        val repeatedLessons: MutableList<StudentDashboardLesson> = mutableListOf(firstDashboardLesson)
        if (repeatsCount > 1) {
            @Suppress("LoopToCallChain")
            for (i in 1..repeatsCount - 1) {
                val repeatedLesson = firstLesson.plusWeeks(i.toLong())
                repeatedLessons.add(studentDashboardLesson(repeatedLesson, visitType, ageGroup, status, cancelType, invalidated, repeatsCount - i))
            }
        }
        return repeatedLessons
    }
}

class LessonTemplateBuilder(val lessonTemplate: LessonTemplate) {
    fun addSlot(day: LessonDay, hours: Int, minutes: Int, subject: LessonSubject, ageGroup: LessonAgeGroup): LessonTemplateBuilder {
        val lessonSlot = TemplateLessonSlot().apply {
            this.day = day
            fromMins = DateTimeUtils.hoursMinutesToMinutes(hours, minutes)
            this.subject = subject
            this.ageGroup = ageGroup
        }
        lessonTemplate.lessons.add(lessonSlot)
        return this
    }

    fun addSlot(dateTime: LocalDateTime, subject: LessonSubject, ageGroup: LessonAgeGroup): LessonTemplateBuilder {
        val lessonSlot = TemplateLessonSlot().apply {
            this.day = LessonDay.fromWeekDay(dateTime.dayOfWeek)
            fromMins = DateTimeUtils.hoursMinutesToMinutes(dateTime.hour, dateTime.minute)
            this.subject = subject
            this.ageGroup = ageGroup
        }
        lessonTemplate.lessons.add(lessonSlot)
        return this
    }

    fun addSlotInFuture(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject, ageGroup: LessonAgeGroup): LessonTemplateBuilder {
        val dateTime = DateTimeUtils.currentDateTime().plusDays(daysDiff).plusHours(hoursDiff).plusMinutes(minutesDiff)
        val lessonSlot = TemplateLessonSlot().apply {
            this.day = LessonDay.fromWeekDay(dateTime.dayOfWeek)
            fromMins = DateTimeUtils.hoursMinutesToMinutes(dateTime.hour, dateTime.minute)
            this.subject = subject
            this.ageGroup = ageGroup
        }
        lessonTemplate.lessons.add(lessonSlot)
        return this
    }

    fun addSlotInPast(daysDiff: Long, hoursDiff: Long, minutesDiff: Long, subject: LessonSubject, ageGroup: LessonAgeGroup): LessonTemplateBuilder {
        val dateTime = DateTimeUtils.currentDateTime().minusDays(daysDiff).minusHours(hoursDiff).minusMinutes(minutesDiff)
        val lessonSlot = TemplateLessonSlot().apply {
            this.day = LessonDay.fromWeekDay(dateTime.dayOfWeek)
            fromMins = DateTimeUtils.hoursMinutesToMinutes(dateTime.hour, dateTime.minute)
            this.subject = subject
            this.ageGroup = ageGroup
        }
        lessonTemplate.lessons.add(lessonSlot)
        return this
    }

    fun asSaveRequest(): LessonTemplateSaveDto {
        return LessonTemplateSaveDto().apply {
            name = lessonTemplate.name
            lessons.addAll(lessonTemplate.lessons)
        }
    }

}