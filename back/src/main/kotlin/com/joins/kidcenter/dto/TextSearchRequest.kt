package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.utils.*
import java.time.LocalDate
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Order

abstract class SearchRequest(
        firstRecord: Int = 1,
        pageRecordsCount: Int = 50
) {
    var firstRecord: Int = firstRecord
    var pageRecordsCount: Int = pageRecordsCount
    var sortColumn: String? = null
    /**
     * Sort order, that is applied when sortColumn is defined.
     * If sortColumn is not defined, value of sortOrder does not affect the order of results.
     */
    var sortOrder: SortOrder = SortOrder.asc

    fun isRecordNumbersValid(): Boolean {
        return firstRecord > 0 && pageRecordsCount > 0
    }

    companion object Factory {

        fun addRecordParameters(request: SearchRequest, parameters: Map<String, String>) {
            request.firstRecord = parameters["firstRecord"]?.toInt(request.firstRecord) ?: request.firstRecord
            request.pageRecordsCount = parameters["pageRecordsCount"]?.toInt(request.pageRecordsCount) ?: request.pageRecordsCount
            request.sortColumn = parameters["sortColumn"]
            request.sortOrder = EnumUtils.valueOf(parameters["sortOrder"], SortOrder.values(), SortOrder.asc)
        }
    }
}

enum class SortOrder {
    asc {
        override fun selectOrder(cb: CriteriaBuilder, expression: Expression<Any?>): Order {
            return cb.asc(expression)
        }
    },
    desc {
        override fun selectOrder(cb: CriteriaBuilder, expression: Expression<Any?>): Order {
            return cb.desc(expression)
        }
    };

    abstract fun selectOrder(cb: CriteriaBuilder, expression: Expression<Any?>): Order
}

open class TextSearchRequest(var text: String = "",
                             firstRecord: Int = 1,
                             pageRecordsCount: Int = 50) :
        SearchRequest(firstRecord, pageRecordsCount) {

    companion object Factory {

        fun fromText(text: String) =
                TextSearchRequest(text)

        fun fromMap(parameters: Map<String, String>?): TextSearchRequest {
            val request = TextSearchRequest()
            if (parameters !== null) {
                SearchRequest.Factory.addRecordParameters(request, parameters)
                request.text = parameters["text"] ?: request.text
            }
            return request
        }
    }
}

open class SelectionByIdRequestPart<out T>(val id: T) {

    companion object {
        fun <T> fromId(id: T?): SelectionByIdRequestPart<T>? {
            return if (id == null) null else SelectionByIdRequestPart(id)
        }
    }
}

class StudentSearchRequest(text: String = "",
                           firstRecord: Int = 1,
                           pageRecordsCount: Int = 50) :
        TextSearchRequest(text, firstRecord, pageRecordsCount) {
    var createdDatePeriod: SearchPeriod = SearchPeriod.all
    var createdDateStart: LocalDate? = null
    var createdDateEnd: LocalDate? = null
    var status: StudentStatus? = null
    var manager: Long? = null
    var selection: SelectionByIdRequestPart<Long>? = null

    fun getEffectiveStartDate(): LocalDate {
        return DateTimeUtils.getRelativeEffectiveStartDate(createdDatePeriod, createdDateStart)
    }

    fun getEffectiveEndDate(): LocalDate {
        return DateTimeUtils.getRelativeEffectiveEndDate(createdDatePeriod, createdDateEnd)
    }

    companion object Factory {
        fun fromMap(parameters: Map<String, String>?): StudentSearchRequest {
            val request = StudentSearchRequest()
            if (parameters !== null) {
                SearchRequest.Factory.addRecordParameters(request, parameters)

                request.selection = SelectionByIdRequestPart.fromId(parameters["selection"].toLongOrNull())
                request.text = parameters["text"] ?: request.text
                val statusStr = parameters["status"]
                request.status = EnumUtils.nullableValueOf(if (statusStr == "all") null else statusStr, StudentStatus.values())
                request.manager = parameters["manager"].toLongOrNull()
                request.createdDatePeriod = EnumUtils.valueOf(parameters["createdDatePeriod"], SearchPeriod.values(), SearchPeriod.all)
                if (request.createdDatePeriod == SearchPeriod.custom) {
                    request.createdDateStart = parameters["createdDateStart"].toLocalDate(DateTimeUtils.currentDate())
                    request.createdDateEnd = parameters["createdDateEnd"].toLocalDate(DateTimeUtils.currentDate().plusMonths(1))
                }
            }
            return request
        }
    }
}

class StudentCallSearchRequest(firstRecord: Int = 1,
                               pageRecordsCount: Int = 50) :
        SearchRequest(firstRecord, pageRecordsCount) {
    var studentId: Long? = null
    var studentStatus: StudentStatus? = null
    var method: StudentCallMethod? = null
    var result: StudentCallResult? = null
    var period: SearchPeriod = SearchPeriod.month
    /**
     * Dates to specify SearchPeriod.custom period
     */
    var periodStart: LocalDate? = null
    var periodEnd: LocalDate? = null

    fun getEffectiveStartDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveStartDate(period, periodStart)
    }

    fun getEffectiveEndDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveEndDate(period, periodEnd)
    }

    companion object Factory {
        fun fromMap(parameters: Map<String, String>?): StudentCallSearchRequest {
            val request = StudentCallSearchRequest()
            if (parameters !== null) {
                SearchRequest.Factory.addRecordParameters(request, parameters)
                request.studentId = parameters["student.id"]?.toLong(0)
                request.studentStatus = EnumUtils.nullableValueOf(parameters["studentStatus"], StudentStatus.values())
                request.method = EnumUtils.nullableValueOf(parameters["method"], StudentCallMethod.values())
                request.result = EnumUtils.nullableValueOf(parameters["result"], StudentCallResult.values())
                request.period = EnumUtils.valueOf(parameters["period"], SearchPeriod.values(), SearchPeriod.month)
                if (request.period == SearchPeriod.custom) {
                    request.periodStart = parameters["periodStart"].toLocalDate(DateTimeUtils.currentDate())
                    request.periodEnd = parameters["periodEnd"].toLocalDate(DateTimeUtils.currentDate().plusMonths(1))
                }
            }
            return request
        }
    }
}


class HomeworkSearchRequest(firstRecord: Int = 1,
                            pageRecordsCount: Int = 50) :
        SearchRequest(firstRecord, pageRecordsCount) {
    var activeDatePeriod: SearchPeriod = SearchPeriod.month
    var startDate: LocalDate? = null
    var endDate: LocalDate? = null
    var subject: LessonSubject? = null
    var ageGroup: LessonAgeGroup? = null
    /**
     * Date which should be between startDate and endDate of the Homework
     */
    var dateInsidePeriod: LocalDate? = null

    fun getEffectiveStartDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveStartDate(activeDatePeriod, startDate)
    }

    fun getEffectiveEndDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveEndDate(activeDatePeriod, endDate)
    }

    companion object Factory {
        fun fromMap(parameters: Map<String, String>?): HomeworkSearchRequest {
            val request = HomeworkSearchRequest()
            if (parameters !== null) {
                SearchRequest.Factory.addRecordParameters(request, parameters)
                request.subject = EnumUtils.nullableValueOf(parameters["subject"], LessonSubject.values())
                request.ageGroup = EnumUtils.nullableValueOf(parameters["ageGroup"], LessonAgeGroup.values())
                request.dateInsidePeriod = parameters["dateInsidePeriod"]?.toLocalDate(DateTimeUtils.currentDate())
                request.activeDatePeriod = EnumUtils.valueOf(parameters["activeDatePeriod"], SearchPeriod.values(), SearchPeriod.month)
                if (request.activeDatePeriod == SearchPeriod.custom) {
                    request.startDate = parameters["startDate"].toLocalDate(DateTimeUtils.currentDate())
                    request.endDate = parameters["endDate"].toLocalDate(DateTimeUtils.currentDate().plusMonths(1))
                }
            }
            return request
        }
    }
}

class CardSearchRequest(activeState: CardActiveState = CardActiveState.active,
                        firstRecord: Int = 1,
                        pageRecordsCount: Int = 50) :
        SearchRequest(firstRecord, pageRecordsCount) {
    var activeState: CardActiveState = activeState
    var visitType: OptionClause<VisitType>? = null
    var ageRange: OptionClause<AgeRange>? = null

    companion object Factory {

        fun fromMap(parameters: Map<String, String>?): CardSearchRequest {
            val request = CardSearchRequest()
            if (parameters !== null) {
                SearchRequest.Factory.addRecordParameters(request, parameters)
                request.activeState = EnumUtils.valueOf(parameters["activeState"], CardActiveState.values(), CardActiveState.active)
                request.visitType = OptionClause.fromString(parameters["visitType"] ?: "", { optionId -> EnumUtils.nullableValueOf(optionId, VisitType.values()) })
                request.ageRange = OptionClause.fromString(parameters["ageRange"] ?: "", { optionId -> EnumUtils.nullableValueOf(optionId, AgeRange.values()) })
            }
            return request
        }
    }
}

enum class CardActiveState {
    active, inactive, all;
}

class PaymentSearchRequest(firstRecord: Int = 1,
                           pageRecordsCount: Int = 50) :
        SearchRequest(firstRecord, pageRecordsCount), Cloneable {
    var searchMethod: PaymentSearchMethod = PaymentSearchMethod.filters
    var direction: PaymentDirection? = null
    var source: PaymentOriginRequestPart = PaymentOriginRequestPart()
    var target: PaymentOriginRequestPart = PaymentOriginRequestPart()
    var anyEndpoint: PaymentOriginRequestPart = PaymentOriginRequestPart()
    var period: SearchPeriod = SearchPeriod.month
    /**
     * Dates to specify SearchPeriod.custom period
     */
    var periodStart: LocalDate? = null
    var periodEnd: LocalDate? = null

    var categoryIds: Collection<Long>? = null
    var useInnerCategories: Boolean = false
    var innerCategoryIds: Collection<Long>? = null
    var groupBy: StatGroupBy = StatGroupBy.none
    var text: String = ""
    var skipPhotos: Boolean = false

    fun getEffectiveCategoryIds(categoriesMap: Map<Long, Category>): Collection<Long>? {
        return (if (useInnerCategories) innerCategoryIds else categoryIds)?.filter {
            val category = categoriesMap[it]
            category != null && (useInnerCategories || category.parent == null)
        }
    }

    fun getEffectiveStartDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveStartDate(period, periodStart)
    }

    fun getEffectiveEndDate(): LocalDate {
        return DateTimeUtils.getAbsoluteEffectiveEndDate(period, periodEnd)
    }

    override public fun clone(): PaymentSearchRequest {
        return super.clone() as PaymentSearchRequest
    }

    companion object Factory {

        fun fromMap(parameters: Map<String, String>?): PaymentSearchRequest {
            val request = PaymentSearchRequest()
            if (parameters !== null) {
                addRecordParameters(request, parameters)
                request.searchMethod = EnumUtils.valueOf(parameters["searchMethod"], PaymentSearchMethod.values(), PaymentSearchMethod.filters)
                request.direction = EnumUtils.nullableValueOf(parameters["direction"], PaymentDirection.values())
                request.source = PaymentOriginRequestPart.fromMap(parameters, "source.")
                request.target = PaymentOriginRequestPart.fromMap(parameters, "target.")
                request.period = EnumUtils.valueOf(parameters["period"], SearchPeriod.values(), SearchPeriod.month)
                if (request.period == SearchPeriod.custom) {
                    request.periodStart = parameters["periodStart"].toLocalDate(DateTimeUtils.currentDate())
                    request.periodEnd = parameters["periodEnd"].toLocalDate(DateTimeUtils.currentDate().plusMonths(1))
                }
                request.categoryIds = parameters["categoryIds"]?.toLongList(",")
                request.useInnerCategories = parameters["useInnerCategories"].toBoolean(false)
                request.innerCategoryIds = parameters["innerCategoryIds"]?.toLongList(",")
                request.groupBy = EnumUtils.valueOf(parameters["groupBy"], StatGroupBy.values(), StatGroupBy.none)
                request.text = parameters["text"] ?: ""
                request.skipPhotos = parameters["skipPhotos"].toBoolean(false)
            }
            return request
        }
    }
}

open class PaymentOriginRequestPart() {
    var schoolId: Long? = null
    var accountIds: Collection<Long>? = null

    companion object Factory {

        fun fromMap(parameters: Map<String, String>?, prefix: String): PaymentOriginRequestPart {
            val request = PaymentOriginRequestPart()
            if (parameters !== null) {
                request.schoolId = parameters["${prefix}schoolId"]?.toLong(0)
                request.accountIds = parameters["${prefix}accountIds"]?.toLongList(",")
            }
            return request
        }
    }
}

class PaymentBalanceRequest() : PaymentOriginRequestPart() {
    var period: SearchPeriod = SearchPeriod.month
    var periodStart: LocalDate? = null
    var periodEnd: LocalDate? = null

    fun getEffectiveBalanceRange(): DateRange {
        val curDate = DateTimeUtils.currentDate()
        return when (period) {
            SearchPeriod.day, SearchPeriod.week, SearchPeriod.month -> DateRange(curDate.withDayOfMonth(1), curDate.withDayOfMonth(curDate.lengthOfMonth()))
            SearchPeriod.year -> DateRange(curDate.withDayOfYear(1), curDate.withDayOfYear(curDate.lengthOfYear()))
            SearchPeriod.all -> DateRange.eternity
            SearchPeriod.custom -> DateRange(periodStart!!.withDayOfMonth(1), periodEnd!!.withDayOfMonth(periodEnd!!.lengthOfMonth()))
        }
    }

    companion object Factory {

        fun fromMap(parameters: Map<String, String>?): PaymentBalanceRequest {
            val request = PaymentBalanceRequest()
            if (parameters !== null) {
                request.schoolId = parameters["schoolId"]?.toLong(0)
                request.accountIds = parameters["accountIds"]?.toLongList(",")

                request.period = EnumUtils.valueOf(parameters["period"], SearchPeriod.values(), SearchPeriod.month)
                if (request.period == SearchPeriod.custom) {
                    request.periodStart = parameters["periodStart"].toLocalDate(DateTimeUtils.currentDate())
                    request.periodEnd = parameters["periodEnd"].toLocalDate(DateTimeUtils.currentDate().plusMonths(1))
                }
            }
            return request
        }
    }
}

enum class PaymentSearchMethod {
    text,
    filters;
}

enum class PaymentDirection {
    outgoing,
    incoming,
    transfer;

    companion object {
        val requiredAccountDirections = listOf(PaymentDirection.outgoing, PaymentDirection.transfer)
        val requiredTargetAccountDirections = listOf(PaymentDirection.incoming, PaymentDirection.transfer)
    }
}

enum class SearchPeriod {
    day,
    week,
    month,
    year,
    custom,
    all;
}

enum class StatGroupBy {
    none,
    category,
    category2,
    category3,
    category4,
    category5;
}

class StudentLessonsSearchRequest {
    var id: Long = 0
    var timeCategory: StudentLessonsTimeCategory = StudentLessonsTimeCategory.schedule
    var visitType: VisitType? = null
    var lessonDate: LocalDate = DateTimeUtils.currentDate()

    companion object Factory {
        fun fromMap(id: Long, parameters: Map<String, String>?): StudentLessonsSearchRequest {
            val request = StudentLessonsSearchRequest()
            request.id = id
            if (parameters !== null) {
                request.timeCategory = EnumUtils.valueOf(parameters["timeCategory"], StudentLessonsTimeCategory.values(), StudentLessonsTimeCategory.schedule)
                request.visitType = EnumUtils.nullableValueOf(parameters["visitType"], VisitType.values())
                request.lessonDate = parameters["lessonDate"].toLocalDate(DateTimeUtils.currentDate())
            }
            return request
        }
    }
}

enum class StudentLessonsTimeCategory {
    schedule, history
}


class OptionClause<T>(val option: T,
                      val negate: Boolean) {

    companion object {
        fun <T> fromString(text: String, optionResolver: (String) -> T?): OptionClause<T>? {
            val negate = text.startsWith("!")
            val optionId = if (negate) text.substring(1) else text
            val option = optionResolver.invoke(optionId)
            return if (option == null) null else OptionClause(option, negate)
        }
    }
}