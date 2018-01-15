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

package com.joins.kidcenter.utils

import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.LessonDay
import com.joins.kidcenter.dto.SearchPeriod
import com.joins.kidcenter.service.lessons.schedule.LessonTime
import org.apache.commons.lang3.Validate
import java.time.*
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    val hoursInDay = 24
    val minutesInHour = 60
    val minutesInDay = hoursInDay * minutesInHour

    fun currentDate(): LocalDate = LocalDate.now(ZoneId.of(Config.currentTimeZone))

    fun currentDateTime(): LocalDateTime = LocalDateTime.now(ZoneId.of(Config.currentTimeZone))

    fun currentDayOfWeek(): LessonDay {
        return LessonDay.fromWeekDay(DateTimeUtils.currentDateTime().dayOfWeek)
    }

    fun dayOfWeekInFuture(days: Long = 0, hours: Long = 0): LessonDay {
        return LessonDay.fromWeekDay(DateTimeUtils.currentDateTime().plusDays(days).plusHours(hours).dayOfWeek)
    }

    fun dayOfWeekInPast(days: Long = 0, hours: Long = 0): LessonDay {
        return LessonDay.fromWeekDay(DateTimeUtils.currentDateTime().minusDays(days).minusHours(hours).dayOfWeek)
    }

    fun hourInFuture(hours: Long): Int {
        return DateTimeUtils.currentDateTime().plusHours(hours).hour
    }

    fun hourInPast(hours: Long): Int {
        return DateTimeUtils.currentDateTime().minusHours(hours).hour
    }

    fun minsFromMidnight(dateTime: LocalDateTime): Int {
        return dateTime.hour * minutesInHour + dateTime.minute
    }

    fun withMinsFromMidnight(date: LocalDate, mins: Int): LocalDateTime {
        Validate.isTrue(mins < minutesInDay, "Minutes value is to big, expected value should be less than $minutesInDay")
        val hours = (mins / minutesInHour)
        val minsInHour = (mins % minutesInHour)
        return date.toLocalDateTimeMidnight().withHour(hours).withMinute(minsInHour)
    }

    fun withLessonTime(date: LocalDate, lessonTime: LessonTime): LocalDateTime {
        return LocalDateTime.of(date, lessonTime.startTime)
    }

    fun nextLessonTime(time: LocalTime): LessonTime? {
        return LessonTime.values().find { it.startTime.isAfter(time) }
    }

    fun nextLessonLocalDateTime(dateTime: LocalDateTime): LocalDateTime {
        val lessonTime = DateTimeUtils.nextLessonTime(dateTime.toLocalTime())
        return if (lessonTime == null)
            DateTimeUtils.withLessonTime(dateTime.toLocalDate().plusDays(1), LessonTime.values()[0])
        else
            DateTimeUtils.withLessonTime(dateTime.toLocalDate(), lessonTime)
    }

    fun minutesToTime(mins: Int): String {
        val hours = (mins / minutesInHour).toString().padStart(2, '0')
        val minsInHour = (mins % minutesInHour).toString().padStart(2, '0')
        return "$hours:$minsInHour"
    }

    fun timeToMinutes(time: String): Int {
        val parts = time.split(':')
        if (parts.size < 2) {
            throw IllegalArgumentException("Unable to parse time $time. It should have format HH:mm")
        }
        val hours: Int? = parts[0].toIntOrNull()
        val minutes: Int? = parts[1].toIntOrNull()

        if (hours == null || minutes == null) {
            throw IllegalArgumentException("Unable to parse time $time. Hours and minutes value should be positive number")
        }

        return hoursMinutesToMinutes(hours, minutes)
    }

    fun hoursMinutesToMinutes(hours: Int, minutes: Int): Int {
        if (hours < 0 || hours >= hoursInDay) {
            throw IllegalArgumentException("Unable to convert hours $hours. Hours value should be positive number and less then $hoursInDay")
        }
        if (minutes < 0 || minutes >= minutesInHour) {
            throw IllegalArgumentException("Unable to convert minutes $minutes. Minutes value should be positive number and less then $minutesInHour")
        }
        return hours * minutesInHour + minutes
    }

    fun lessonDateTimeFromString(dateTimeStr: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern(Config.datetTimePattern)
        return LocalDateTime.parse(dateTimeStr, formatter)
    }

    fun lessonDateTimeToString(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern(Config.datetTimePattern)
        return formatter.format(dateTime)
    }

    fun dateFromString(dateStr: String, pattern: String = Config.datePattern): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDate.parse(dateStr, formatter)
    }

    fun dateToString(date: LocalDate, pattern: String = Config.datePattern): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return formatter.format(date)
    }

    fun min(date1: LocalDate, date2: LocalDate) =
            if (date1.isBefore(date2)) date1 else date2

    fun max(date1: LocalDate, date2: LocalDate) =
            if (date1.isAfter(date2)) date1 else date2

    fun getAbsoluteEffectiveStartDate(period: SearchPeriod, startDate: LocalDate?): LocalDate {
        val currentDate = DateTimeUtils.currentDate()
        return when (period) {
            SearchPeriod.day -> currentDate
            SearchPeriod.week -> currentDate.minusDays(currentDate.dayOfWeek.ordinal.toLong())
            SearchPeriod.month -> currentDate.withDayOfMonth(1)
            SearchPeriod.year -> currentDate.withDayOfYear(1)
            SearchPeriod.custom -> startDate!!
            SearchPeriod.all -> Config.minDate
        }
    }

    fun getAbsoluteEffectiveEndDate(period: SearchPeriod, endDate: LocalDate?): LocalDate {
        val currentDate = DateTimeUtils.currentDate()
        return when (period) {
            SearchPeriod.day -> currentDate
            SearchPeriod.week -> currentDate.plusDays((DayOfWeek.SUNDAY.ordinal - currentDate.dayOfWeek.ordinal).toLong())
            SearchPeriod.month -> currentDate.withDayOfMonth(currentDate.lengthOfMonth())
            SearchPeriod.year -> currentDate.withDayOfYear(currentDate.lengthOfYear())
            SearchPeriod.custom -> endDate!!
            SearchPeriod.all -> Config.maxDate
        }
    }

    fun getRelativeEffectiveStartDate(period: SearchPeriod, startDate: LocalDate?): LocalDate {
        val currentDate = DateTimeUtils.currentDate()
        return when (period) {
            SearchPeriod.day -> currentDate
            SearchPeriod.week -> currentDate.minusDays(6)
            SearchPeriod.month -> currentDate.minusDays(30)
            SearchPeriod.year -> currentDate.minusDays(365)
            SearchPeriod.custom -> startDate!!
            SearchPeriod.all -> Config.minDate
        }
    }

    fun getRelativeEffectiveEndDate(period: SearchPeriod, endDate: LocalDate?): LocalDate {
        val currentDate = DateTimeUtils.currentDate()
        return when (period) {
            SearchPeriod.day -> currentDate
            SearchPeriod.week -> currentDate
            SearchPeriod.month -> currentDate
            SearchPeriod.year -> currentDate
            SearchPeriod.custom -> endDate!!
            SearchPeriod.all -> Config.maxDate
        }
    }
}
