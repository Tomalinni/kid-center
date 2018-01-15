package com.joins.kidcenter.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.joins.kidcenter.Config
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SaveResult
import org.springframework.http.HttpStatus
import java.io.OutputStream
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.servlet.http.HttpServletResponse


fun String?.toDouble(defaultValue: Double): Double {
    if (this == null) return defaultValue
    try {
        return java.lang.Double.parseDouble(this)
    } catch(e: NumberFormatException) {
    }
    return defaultValue
}

fun String?.toLong(defaultValue: Long): Long {
    return toLongNullable(this, defaultValue)!!
}

fun String?.toLongOrNull(): Long? {
    return toLongNullable(this, null)
}

private fun toLongNullable(str: String?, defaultValue: Long?): Long? {
    if (str == null) return defaultValue
    try {
        return java.lang.Long.parseLong(str)
    } catch(e: NumberFormatException) {
    }
    return defaultValue
}

fun String?.toInt(defaultValue: Int): Int {
    if (this == null) return defaultValue
    try {
        return java.lang.Integer.parseInt(this)
    } catch(e: NumberFormatException) {
    }
    return defaultValue
}

fun String?.toLocalDate(defaultValue: LocalDate): LocalDate {
    if (this == null) return defaultValue
    try {
        return DateTimeUtils.dateFromString(this)
    } catch(e: DateTimeParseException) {
    }
    return defaultValue
}

fun String?.toBoolean(defaultValue: Boolean): Boolean =
        if (this == null) defaultValue else this.toBoolean()

fun String?.toLongArray(separator: String): Array<Long> {
    if (this == null) return arrayOf()
    return toLongList(separator)
            .toTypedArray()
}

fun String?.toLongList(separator: String): List<Long> {
    if (this == null) return listOf()
    return this.split(separator)
            .filter { !it.isBlank() }
            .map { it.toLong(-1) }
            .filter { it != -1L }
}

fun <T> T?.ensure(accepted: Collection<T>, defaultVal: T): T {
    return if (this != null && accepted.contains(this)) this else defaultVal
}

fun HttpServletResponse.sendStatus(status: HttpStatus) =
        sendError(status.value(), status.reasonPhrase)

fun HttpServletResponse.sendObject(mapper: ObjectMapper, obj: Any?, provider: FilterProvider? = null) =
        if (obj == null) sendStatus(HttpStatus.NOT_FOUND) else sendObject(mapper.writer().with(provider), obj)

fun HttpServletResponse.sendObject(writer: ObjectWriter, obj: Any, status: HttpStatus = HttpStatus.OK) {
    setStatus(status.value())
    writer.writeValue(outputStream, obj)
}

fun HttpServletResponse.sendSaveResult(mapper: ObjectMapper, result: SaveResult<*>, provider: FilterProvider? = null) {
    if (result.success()) {
        if (result.obj != null) {
            this.sendObject(mapper.writer().with(provider), result.obj, HttpStatus.OK)
        } else {
            status = HttpStatus.OK.value()
        }
    } else {
        this.sendObject(mapper.writer().with(provider), result.validationMessages, HttpStatus.BAD_REQUEST)
    }
}

fun HttpServletResponse.sendDeleteResult(mapper: ObjectMapper, result: DeleteResult<*>, provider: FilterProvider? = null) {
    this.sendObject(mapper.writer().with(provider), result, if (result.success()) HttpStatus.OK else HttpStatus.BAD_REQUEST)
}

fun HttpServletResponse.sendFile(fileName: String, contentType: String, generator: (out: OutputStream) -> Unit) {
    addHeader("Content-Disposition", "inline; filename=\"$fileName\"")
    this.contentType = contentType
    generator(outputStream)
}

fun LocalDate.toSqlDate() =
        java.sql.Date.valueOf(this)


fun LocalDateTime.toSqlTimestamp() =
        java.sql.Timestamp.valueOf(this)

fun java.sql.Date?.toLocalDate(): LocalDate? {
    return if (this == null) null else this.toLocalDate()!!
}

fun java.util.Date?.toLocalDateTime(): LocalDateTime? {
    return if (this == null) null else LocalDateTime.ofInstant(this.toInstant(), ZoneId.of(Config.currentTimeZone))
}

fun LocalDateTime?.toDate(): java.util.Date? {
    return if (this == null) null else Date.from(this.atZone(ZoneId.of(Config.currentTimeZone)).toInstant())
}

fun CriteriaBuilder.trueClause() = this.and()

fun CriteriaBuilder.falseClause() = this.or()

fun CriteriaBuilder.ilike(expression: Expression<String>, text: String) =
        if (text.isBlank()) this.trueClause() else this.like(this.lower(expression), "%${text.toLowerCase()}%")

fun <T> CriteriaBuilder.safeEqual(expression: Expression<T>, value: T?) =
        if (value == null) this.isNull(expression) else this.equal(expression, value)

fun LocalDate.toLocalDateTimeMidnight() =
        LocalDateTime.of(this, LocalTime.MIDNIGHT)

fun LocalDate.toLocalDateTimeLastDayMoment() =
        LocalDateTime.of(this, LocalTime.MAX)

fun <T : Comparable<T>> Iterable<T>.limits(): Pair<T, T>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    val elem = iterator.next()
    var max = elem
    var min = elem

    while (iterator.hasNext()) {
        val e = iterator.next()
        if (max < e) max = e
        if (min > e) min = e
    }
    return Pair(min, max)
}


fun LocalDateTime.withDayOfWeekInPast(day: DayOfWeek): LocalDateTime {
    return this.minusDays(getDaysDiffForPast(day, dayOfWeek))
}

fun LocalDateTime.withDayOfWeekInPastOrToday(day: DayOfWeek): LocalDateTime {
    return this.minusDays(getDaysDiffForPast(day, dayOfWeek) % 7)
}

private fun getDaysDiffForPast(day: DayOfWeek, currentDay: DayOfWeek): Long {
    val currentDayIndex = currentDay.ordinal
    var dayDiff = currentDayIndex - day.ordinal
    if (dayDiff <= 0) { //move to previous week
        dayDiff += 7
    }
    val dayDiffLong = dayDiff.toLong()
    return dayDiffLong
}

fun LocalDateTime.withDayOfWeekInFuture(day: DayOfWeek): LocalDateTime {
    return this.plusDays(getDaysDiffForFuture(day, dayOfWeek))
}

fun LocalDateTime.withDayOfWeekInFutureOrToday(day: DayOfWeek): LocalDateTime {
    return this.plusDays(getDaysDiffForFuture(day, dayOfWeek) % 7)
}

private fun getDaysDiffForFuture(day: DayOfWeek, currentDay: DayOfWeek): Long {
    val currentDayIndex = currentDay.ordinal
    var dayDiff = day.ordinal - currentDayIndex
    if (dayDiff <= 0) { //move to previous week
        dayDiff += 7
    }
    val dayDiffLong = dayDiff.toLong()
    return dayDiffLong
}

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {
            e.addSuppressed(closeException)
        }
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

