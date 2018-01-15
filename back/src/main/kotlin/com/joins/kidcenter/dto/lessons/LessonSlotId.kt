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

package com.joins.kidcenter.dto.lessons

import com.fasterxml.jackson.annotation.JsonValue
import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.LessonDay
import com.joins.kidcenter.domain.LessonSlot
import com.joins.kidcenter.domain.LessonSubject
import com.joins.kidcenter.domain.TemplateLessonSlot
import com.joins.kidcenter.utils.DateTimeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LessonSlotId constructor(val dateTime: LocalDateTime, val subject: LessonSubject)
    : Comparable<LessonSlotId> {

    val day: LessonDay = LessonDay.values()[dateTime.dayOfWeek.ordinal]
    val time: LocalTime = dateTime.toLocalTime()
    val id: String = subject.prefix + DateTimeUtils.lessonDateTimeToString(dateTime)

    companion object {
        private val idLength = 16

        fun fromString(id: String): LessonSlotId {
            if (id.length != idLength) {
                throw IllegalArgumentException("Lesson id $id has illegal length. ${lessonIdFormatMessage()}")
            }
            val subjectPrefix = id.substring(0, 1)
            val lessonSubject = LessonSubject.fromPrefix(subjectPrefix)
            if (lessonSubject == null) {
                throw IllegalArgumentException("Lesson id $id has illegal subject prefix $subjectPrefix. ${lessonIdFormatMessage()}")
            }
            try {
                val lessonDateTime = DateTimeUtils.lessonDateTimeFromString(id.substring(1, id.length))
                return LessonSlotId(lessonDateTime, lessonSubject)
            } catch(e: DateTimeParseException) {
                throw IllegalArgumentException("Lesson id $id has illegal date and time part. ${e.message}. ${lessonIdFormatMessage()}")
            }
        }

        fun fromTemplateSlot(slot: TemplateLessonSlot, date: LocalDate): LessonSlotId {
            val day = LessonDay.fromWeekDay(date.dayOfWeek)
            if (slot.day != day) {
                throw IllegalArgumentException("Lesson date does not match with specified template slot by week day")
            }
            return LessonSlotId(DateTimeUtils.withMinsFromMidnight(date, slot.fromMins), slot.subject)
        }

        fun fromLesson(lessonSlot: LessonSlot): LessonSlotId {
            return LessonSlotId(lessonSlot.dateTime, lessonSlot.subject)
        }

        private fun lessonIdFormatMessage() =
                "Valid lesson id format is Sdd.MM.yyyy-HHmm"
    }

    @JsonValue
    fun id() = id

    override fun toString(): String = id()

    fun relativeId(): String {
        val subjectOrd = subject.ordinal
        val dayOrd = day.ordinal
        val time = time.format(DateTimeFormatter.ofPattern(Config.timePattern))
        return "$subjectOrd-$dayOrd-$time"
    }

    fun withDateTime(dateTime: LocalDateTime): LessonSlotId =
            LessonSlotId(dateTime, this.subject)

    fun plusWeeks(weeks: Long = 1): LessonSlotId = withDateTime(dateTime.plusWeeks(weeks))

    fun andFollowingWeeks(weeks: Long = 1): LessonSlotIds {
        var currentId = this
        val currentLessonIds: MutableList<LessonSlotId> = mutableListOf(currentId)
        for (i in 0..weeks - 1) {
            currentId = currentId.plusWeeks(1)
            currentLessonIds.add(currentId)
        }
        return LessonSlotIds(currentLessonIds)
    }

    fun matchesTemplate(templateLesson: TemplateLessonSlot): Boolean {
        return dateTime.dayOfWeek.ordinal == templateLesson.day.ordinal &&
                DateTimeUtils.minsFromMidnight(dateTime) == templateLesson.fromMins &&
                subject == templateLesson.subject
    }

    fun hasSameDayTimeAndSubject(lessonId: LessonSlotId): Boolean {
        return hasSameDayAndTime(lessonId) && subject == lessonId.subject
    }

    fun hasSameDayAndTime(lessonId: LessonSlotId): Boolean {
        return day == lessonId.day && time == lessonId.time
    }

    override fun compareTo(other: LessonSlotId): Int {
        var result = dateTime.compareTo(other.dateTime)
        if (result == 0) {
            result = subject.ordinal - other.subject.ordinal
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other is LessonSlotId && id() == other.id()
    }

    override fun hashCode(): Int {
        return id().hashCode()
    }
}

class LessonSlotIds(lessonIds: Collection<LessonSlotId>) {

    val lessonIds: Set<LessonSlotId> = lessonIds.toHashSet()

    fun minDateTime(): LocalDateTime? = lessonIds.map { it.dateTime }.min()

    fun maxDateTime(): LocalDateTime? = lessonIds.map { it.dateTime }.max()

    fun plus(lessonSlotIds: LessonSlotIds): LessonSlotIds {
        return LessonSlotIds(this.lessonIds.plus(lessonSlotIds.lessonIds))
    }

    /**
     * Find groups of lessons ids with same day of week and time
     */
    fun conflicts(): List<LessonSlotIds> {
        val conflictGroups: MutableList<LessonSlotIds> = mutableListOf()
        val conflictIds: MutableList<LessonSlotId> = mutableListOf() //holds ids that are in conflict with others
        val idsArray = lessonIds.toTypedArray()

        for (i in 0..idsArray.lastIndex - 1) {
            var conflictGroup: MutableList<LessonSlotId>? = null
            val id = idsArray[i]

            if (!conflictIds.contains(id)) { //no conflicts with already handled ids were detected
                for (j in i + 1..idsArray.lastIndex) {
                    val candidateId = idsArray[j]
                    if (id.hasSameDayAndTime(candidateId)) {

                        if (conflictGroup == null) {
                            conflictGroup = mutableListOf(id)
                            conflictGroups.add(LessonSlotIds(conflictGroup))
                            conflictIds.add(id)
                        }
                        conflictGroup.add(candidateId)
                        conflictIds.add(candidateId)
                    }
                }
            }
        }
        return conflictGroups.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as LessonSlotIds

        if (lessonIds != other.lessonIds) return false

        return true
    }

    override fun hashCode(): Int {
        return lessonIds.hashCode()
    }

    override fun toString(): String {
        return lessonIds.joinToString(prefix = "[", postfix = "]")
    }

    companion object {
        fun fromString(ids: Collection<String>): LessonSlotIds {
            return LessonSlotIds(ids.map { LessonSlotId.fromString(it) })
        }
    }
}
