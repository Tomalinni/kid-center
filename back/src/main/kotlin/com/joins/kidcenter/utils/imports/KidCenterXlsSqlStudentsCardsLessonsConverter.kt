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

package com.joins.kidcenter.utils.imports

import com.github.excelmapper.core.engine.*
import com.github.excelmapper.core.engine.CellDefinitions.fromReferences
import com.github.excelmapper.core.engine.References.property
import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.lessons.LessonSlotId
import com.joins.kidcenter.service.persistence.StudentCodeTransformer
import com.joins.kidcenter.utils.*
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.aggregator
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.bookedLessons
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.existingRegularBusinessIdsToIds
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.existingTrialBusinessIdsToIds
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.hibernateSeq
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.managerIds
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.mobilePhones
import com.joins.kidcenter.utils.imports.KidCenterXlsSqlStudentsCardsLessonsConverter.Companion.templateLessonSlots
import org.apache.commons.lang3.StringUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.sql.Connection
import java.sql.DriverManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*

class KidCenterXlsSqlStudentsCardsLessonsConverter {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            KidCenterXlsSqlStudentsCardsLessonsConverter().convert()
        }

        var hibernateSeq: SqlSequence? = null
        var studentCardSeq: SqlSequence? = null
        val mobilePhones: MutableMap<String, MutableList<String>> = mutableMapOf()

        val managerIds: List<Long> = listOf(680, 682, 708)
        val existingTrialBusinessIdsToIds: MutableMap<String, Long> = mutableMapOf()
        val existingRegularBusinessIdsToIds: MutableMap<String, Long> = mutableMapOf()
        val bookedLessons: MutableMap<LessonSlotId, Int> = mutableMapOf()
        val templateLessonSlots: MutableList<TemplateLessonSlot> = mutableListOf()

        val aggregator: Aggregator = Aggregator()
    }

    val importFilePath = "data/kids-cards-lessons.xlsx"
    val sqlFilePath = "data/kids-cards-lessons.sql"
    val importedColumns = arrayOf("regularBusinessId", "trialBusinessId", "nameEn", "nameCn", "birthDay", "gender",
            "fatherName", "fatherPhone",
            "motherName", "motherPhone",
            "auntName", "auntPhone",
            "grandmaName", "grandmaPhone",
            "card1PurchaseDate", "card1ExpirationDate", "card1SoldLessons", "card1FreeLessons", "card1Price",
            "card2PurchaseDate", "card2ExpirationDate", "card2SoldLessons", "card2FreeLessons", "card2Price",
            "card3PurchaseDate", "card3ExpirationDate", "card3SoldLessons", "card3FreeLessons", "card3Price",
            "v2015_10", "v2015_11", "v2015_12",
            "v2016_01", "v2016_02", "v2016_03", "v2016_04", "v2016_05", "v2016_06", "v2016_07", "v2016_08", "v2016_09", "v2016_10", "v2016_11", "v2016_12",
            "v2017_01", "v2017_02", "v2017_03", "v2017_04", "v2017_05", "v2017_06", "v2017_07"
    )
    val group: CellGroup = CellGroup().addCells(fromReferences(importedColumns.map(::property)))
    private val studentCodeTransformer = StudentCodeTransformer()
    private var studentNumericBusinessId = StudentCodeTransformer.maxId.toLong() - 997

    private val sqlProlog = "delete from studentslot ss using student s where ss.student_id = s.id and ss.visittype != 1 and s.businessid like 'A%';\n" +
            "delete from studentcard sc using student s where sc.student_id = s.id and sc.visittype != 1 and s.businessid like 'A%';\n"

    fun convert() {
        fillDbData()

        var maxRegularStudentId: Int = 0
        var input: InputStream? = null
        var output: Writer? = null
        try {
            input = FileInputStream(File(importFilePath))
            output = BufferedWriter(FileWriter(File(sqlFilePath)))
            output.write(sqlProlog)

            val wb = XSSFWorkbook(input)
            val sheet = wb.getSheetAt(0)

            val factory = ItemContainerFactory()
            val container = factory.createItemContainer(sheet, CellCoordinate(1, 5))
            var studentRow = readStudent(container)

            while (!studentRow.isEmpty()) {
                if (studentRow.isValid()) {
                    if (studentRow.trialBusinessId.isEmpty() && existingRegularBusinessIdsToIds[studentRow.regularBusinessId] == null) {
                        studentRow.trialBusinessId = generateTrialIdFromEnd()
                        studentRow.toCreateInDb = true
                    }
                    studentRow.registerPhones()
                    studentRow.aggregateLessonsPeriods()
                    maxRegularStudentId = Math.max(maxRegularStudentId, studentRow.regularBusinessId.replace("A", "").toInt(0))
                    val insertedRows = studentRow.toSqlInsertStatement()
                    output.write(insertedRows)
                }

                studentRow = readStudent(container)
            }
            reportNonUniquePhones()
            reportOptimalLessonsPeriod()

            output.write(hibernateSeq!!.getSetValueSql())
            output.write(studentCardSeq!!.getSetValueSql())
            output.write("select setval('public.regular_id_seq', $maxRegularStudentId);\n")
        } finally {
            if (input != null) {
                input.close()
            }
            if (output != null) {
                output.close()
            }
        }
    }

    private fun generateTrialIdFromEnd() = studentCodeTransformer.stringifyTrialId(studentNumericBusinessId--)

    private fun readStudent(container: ItemContainer): KcstlImportStudentRow {
        val studentRow = KcstlImportStudentRow(container.currentCoordinate.row)
        container.readItem(studentRow, group, SimpleProcessMessagesHolder())
        return studentRow.preprocess()
    }

    private fun reportNonUniquePhones() {
        println("\nNon unique phones:")
        mobilePhones.forEach { phone, businessIds ->
            if (businessIds.size > 1) {
                println("Phone: $phone, students: ${businessIds.joinToString()} ")
            }
        }
    }

    private fun reportOptimalLessonsPeriod() {
        println("\nOptimal lessons period in days:")
        println("${aggregator.avg()} days from ${aggregator.count()} results")
    }

    private fun fillDbData() {
        JdbcConnectionManager().doWithConnection { connection ->
            hibernateSeq = initSequenceValue(connection, "public", "hibernate_sequence")
            studentCardSeq = initSequenceValue(connection, "public", "student_card_seq")

            selectBookedLessonIds(connection, { lessonSlotId, studentSlotId ->
                val lessonSlot = LessonSlotId.fromString(lessonSlotId)
                bookedLessons[lessonSlot] = if (studentSlotId == null) 0 else (bookedLessons[lessonSlot] ?: 0) + 1
            })

            selectTemplateLessonSlots(connection, {
                templateLessonSlots.add(it)
            })

            selectStudentIds(connection, {
                existingTrialBusinessIdsToIds.put(it.first, it.third)
                existingRegularBusinessIdsToIds.put(it.second, it.third)
            })

        }
    }

    private fun initSequenceValue(connection: Connection, schema: String, sequence: String): SqlSequence {
        connection.createStatement().use { statement ->
            statement.executeQuery("select nextval('$schema.$sequence')").use { resultSet ->
                if (resultSet.next()) {
                    return SqlSequence(schema, sequence, resultSet.getLong(1))
                } else {
                    throw IllegalStateException("Selecting current value of sequence returned empty result set")
                }
            }
        }
    }

    private fun selectBookedLessonIds(connection: Connection, action: (String, Long?) -> Unit) {
        connection.createStatement().use { statement ->
            statement.executeQuery("select ls.id, ss.id from lessonslot ls left join studentslot ss on ss.lessonslot_id=ls.id").use { resultSet ->
                while (resultSet.next()) {
                    action.invoke(resultSet.getString(1), resultSet.getLong(2))
                }
            }
        }
    }

    private fun selectTemplateLessonSlots(connection: Connection, action: (TemplateLessonSlot) -> Unit) {
        connection.createStatement().use { statement ->
            statement.executeQuery("select id, agegroup, day, frommins, subject from templatelessonslot").use { resultSet ->
                while (resultSet.next()) {
                    action.invoke(TemplateLessonSlot().apply {
                        id = resultSet.getLong(1)
                        ageGroup = LessonAgeGroup.values()[resultSet.getInt(2)]
                        day = LessonDay.values()[resultSet.getInt(3)]
                        fromMins = resultSet.getInt(4)
                        subject = LessonSubject.values()[resultSet.getInt(5)]
                    })
                }
            }
        }
    }

    private fun selectStudentIds(connection: Connection, action: (Triple<String, String, Long>) -> Unit) {
        connection.createStatement().use { statement ->
            statement.executeQuery("select trialbusinessid, businessid, id from student").use { resultSet ->
                while (resultSet.next()) {
                    action.invoke(Triple(resultSet.getString(1), resultSet.getString(2), resultSet.getLong(3)))
                }
            }
        }
    }
}

class KcstlImportStudentRow(var rowNumber: Int = 0,
                            var toCreateInDb: Boolean = false,
                            var regularBusinessId: String = "",
                            var trialBusinessId: String = "",
                            var nameEn: String = "",
                            var nameCn: String = "",
                            var birthDay: String = "",
                            var gender: String = "",
                            var fatherName: String = "",
                            var fatherPhone: String = "",
                            var motherName: String = "",
                            var motherPhone: String = "",
                            var auntName: String = "",
                            var auntPhone: String = "",
                            var grandmaName: String = "",
                            var grandmaPhone: String = "",
                            var card1PurchaseDate: String = "",
                            var card1ExpirationDate: String = "",
                            var card1SoldLessons: String = "",
                            var card1FreeLessons: String = "",
                            var card1Price: String = "",
                            var card2PurchaseDate: String = "",
                            var card2ExpirationDate: String = "",
                            var card2SoldLessons: String = "",
                            var card2FreeLessons: String = "",
                            var card2Price: String = "",
                            var card3PurchaseDate: String = "",
                            var card3ExpirationDate: String = "",
                            var card3SoldLessons: String = "",
                            var card3FreeLessons: String = "",
                            var card3Price: String = "",
                            var v2015_10: String = "",
                            var v2015_11: String = "",
                            var v2015_12: String = "",
                            var v2016_01: String = "",
                            var v2016_02: String = "",
                            var v2016_03: String = "",
                            var v2016_04: String = "",
                            var v2016_05: String = "",
                            var v2016_06: String = "",
                            var v2016_07: String = "",
                            var v2016_08: String = "",
                            var v2016_09: String = "",
                            var v2016_10: String = "",
                            var v2016_11: String = "",
                            var v2016_12: String = "",
                            var v2017_01: String = "",
                            var v2017_02: String = "",
                            var v2017_03: String = "",
                            var v2017_04: String = "",
                            var v2017_05: String = "",
                            var v2017_06: String = "",
                            var v2017_07: String = ""
) {

    private val xlsDatePattern = "d.M.yyyy"
    private val xlsDateFormat = SimpleDateFormat(xlsDatePattern)
    private val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val regularBusinessIdPattern = "A\\d{3}"
    private val trialBusinessIdPattern = "\\d{3}Z"
    private val mobilePattern = "\\d{11}"
    private val visitedLessonIds: MutableSet<LessonSlotId> = mutableSetOf()

    fun preprocess(): KcstlImportStudentRow =
            KcstlImportStudentRow(
                    rowNumber,
                    false,
                    StringUtils.trimToEmpty(regularBusinessId),
                    StringUtils.trimToEmpty(preprocessTrialBusinessId(trialBusinessId)),
                    StringUtils.trimToEmpty(nameEn),
                    StringUtils.trimToEmpty(nameCn),
                    StringUtils.trimToEmpty(birthDay),
                    StringUtils.trimToEmpty(gender),
                    StringUtils.trimToEmpty(fatherName),
                    StringUtils.trimToEmpty(preprocessPhone(fatherPhone)),
                    StringUtils.trimToEmpty(motherName),
                    StringUtils.trimToEmpty(preprocessPhone(motherPhone)),
                    StringUtils.trimToEmpty(auntName),
                    StringUtils.trimToEmpty(preprocessPhone(auntPhone)),
                    StringUtils.trimToEmpty(grandmaName),
                    StringUtils.trimToEmpty(preprocessPhone(grandmaPhone)),
                    StringUtils.trimToEmpty(card1PurchaseDate),
                    StringUtils.trimToEmpty(card1ExpirationDate),
                    StringUtils.trimToEmpty(card1SoldLessons),
                    StringUtils.trimToEmpty(card1FreeLessons),
                    StringUtils.trimToEmpty(card1Price),
                    StringUtils.trimToEmpty(card2PurchaseDate),
                    StringUtils.trimToEmpty(card2ExpirationDate),
                    StringUtils.trimToEmpty(card2SoldLessons),
                    StringUtils.trimToEmpty(card2FreeLessons),
                    StringUtils.trimToEmpty(card2Price),
                    StringUtils.trimToEmpty(card3PurchaseDate),
                    StringUtils.trimToEmpty(card3ExpirationDate),
                    StringUtils.trimToEmpty(card3SoldLessons),
                    StringUtils.trimToEmpty(card3FreeLessons),
                    StringUtils.trimToEmpty(card3Price),
                    StringUtils.trimToEmpty(v2015_10),
                    StringUtils.trimToEmpty(v2015_11),
                    StringUtils.trimToEmpty(v2015_12),
                    StringUtils.trimToEmpty(v2016_01),
                    StringUtils.trimToEmpty(v2016_02),
                    StringUtils.trimToEmpty(v2016_03),
                    StringUtils.trimToEmpty(v2016_04),
                    StringUtils.trimToEmpty(v2016_05),
                    StringUtils.trimToEmpty(v2016_06),
                    StringUtils.trimToEmpty(v2016_07),
                    StringUtils.trimToEmpty(v2016_08),
                    StringUtils.trimToEmpty(v2016_09),
                    StringUtils.trimToEmpty(v2016_10),
                    StringUtils.trimToEmpty(v2016_11),
                    StringUtils.trimToEmpty(v2016_12),
                    StringUtils.trimToEmpty(v2017_01),
                    StringUtils.trimToEmpty(v2017_02),
                    StringUtils.trimToEmpty(v2017_03),
                    StringUtils.trimToEmpty(v2017_04),
                    StringUtils.trimToEmpty(v2017_05),
                    StringUtils.trimToEmpty(v2017_06),
                    StringUtils.trimToEmpty(v2017_07)
            )

    fun preprocessPhone(phone: String): String {
        return phone.replace(Regex("\\D"), "")
    }

    fun preprocessTrialBusinessId(businessId: String): String {
        try {
            val numericBusinessId = businessId.toLowerCase().replace("z", "").toInt()
            return "$numericBusinessId".padStart(3, '0') + 'Z'
        } catch(e: NumberFormatException) {
            return ""
        }
    }

    fun isEmpty(): Boolean =
            arrayOf(nameEn, nameCn, birthDay, gender,
                    fatherName, fatherPhone,
                    motherName, motherPhone,
                    auntName, auntPhone,
                    grandmaName, grandmaPhone,
                    card1PurchaseDate, card1ExpirationDate, card1SoldLessons, card1FreeLessons, card1Price,
                    card2PurchaseDate, card2ExpirationDate, card2SoldLessons, card2FreeLessons, card2Price,
                    card3PurchaseDate, card3ExpirationDate, card3SoldLessons, card3FreeLessons, card3Price,
                    v2015_10, v2015_11, v2015_12,
                    v2016_01, v2016_02, v2016_03, v2016_04, v2016_05, v2016_06, v2016_07, v2016_08, v2016_09, v2016_10, v2016_11, v2016_12,
                    v2017_01, v2017_02, v2017_03, v2017_04, v2017_05, v2017_06, v2017_07)
                    .all { StringUtils.isEmpty(it) }

    fun isValid(): Boolean {
        var valid = true
        if (!regularBusinessId.matches(Regex(regularBusinessIdPattern))) {
            reportWarning("Invalid business id $regularBusinessId, should match $regularBusinessIdPattern")
            valid = false
        }

        if (trialBusinessId.isNotEmpty()) {
            if (!trialBusinessId.matches(Regex(trialBusinessIdPattern))) {
                reportWarning("Invalid business id $trialBusinessId, should match $trialBusinessIdPattern")
                valid = false
            }

            if (existingTrialBusinessIdsToIds[trialBusinessId] == null) {
                reportWarning("Invalid business id $trialBusinessId, should exist in database")
                valid = false
            }
        }

        if (nameCn.isEmpty() && nameEn.isEmpty()) {
            reportWarning("Names en and cn are empty")
            valid = false
        }

        if (!validateDate("birth", birthDay, 2005, 2017)) {
            valid = false
        }

        if (hasFather() && !validateRelative("Father", fatherName, fatherPhone)) {
            valid = false
        }

        if (hasMother() && !validateRelative("Mother", motherName, motherPhone)) {
            valid = false
        }

        if (hasAunt() && !validateRelative("Aunt", auntName, auntPhone)) {
            valid = false
        }

        if (hasGrandma() && !validateRelative("Grandma", grandmaName, grandmaPhone)) {
            valid = false
        }

        if (hasCard1() && !validateCard("card1", card1PurchaseDate, card1ExpirationDate, card1SoldLessons, card1FreeLessons, card1Price)) {
            valid = false
        }

        if (hasCard2() && !validateCard("card2", card2PurchaseDate, card2ExpirationDate, card2SoldLessons, card2FreeLessons, card2Price)) {
            valid = false
        }

        if (hasCard3() && !validateCard("card3", card3PurchaseDate, card3ExpirationDate, card3SoldLessons, card3FreeLessons, card3Price)) {
            valid = false
        }

        return valid
    }

    fun validateRelative(relativeType: String, name: String, phone: String): Boolean {
        if (name.isEmpty()) {
            reportWarning("$relativeType name is empty")
            return false
        }

        if (phone.isNotEmpty() && !phone.matches(Regex(mobilePattern))) {
            reportWarning("$relativeType phone $phone is invalid, should match $mobilePattern")
            return false
        }
        return true
    }

    fun validateCard(cardType: String, purchaseDate: String, expirationDate: String, soldLessons: String, freeLessons: String, price: String): Boolean {
        var valid = true

        val validPurchaseDate = validateDate("$cardType purchase", purchaseDate, 2015, 2017)
        if (!validPurchaseDate) {
            valid = false
        }

        val validExpirationDate = validateDate("$cardType expiration", expirationDate, 2015, 2022)
        if (!validExpirationDate) {
            valid = false
        }

        if (validPurchaseDate && validExpirationDate) {
            val purchaseDateObj = getDate("$cardType purchase", purchaseDate)
            val expirationDateObj = getDate("$cardType expiration", expirationDate)
            if (expirationDateObj!!.isBefore(purchaseDateObj)) {
                reportWarning("$cardType, expiration date is before purchase date")
                valid = false
            }
        }

        if (!validateCount("$cardType sold lessons", soldLessons)) {
            valid = false
        }

        if (!validateCount("$cardType free lessons", freeLessons)) {
            valid = false
        }

        if (!validateCount("$cardType price", price)) {
            valid = false
        }

        return valid
    }

    fun validateCount(countType: String, countStr: String): Boolean {
        if (!countStr.isEmpty()) {
            try {
                val count = Integer(countStr)
                if (count < 0) {
                    reportWarning("$countType value $count is invalid, should be positive")
                    return false
                }
            } catch (e: NumberFormatException) {
                reportWarning("$countType value $countStr is invalid, should be integer number")
                return false
            }
        }
        return true
    }

    fun validateDate(dateType: String, dateStr: String, minYear: Int, maxYear: Int): Boolean {
        val date = getDate(dateType, dateStr)
        if (date == null) {
            return false
        }
        if (date.year < minYear || date.year > maxYear) {
            reportWarning("Suspecting $dateType date $dateStr")
            return false
        }
        return true
    }

    private fun getDate(dateType: String, dateStr: String): LocalDate? {
        if (dateStr.isEmpty()) {
            reportWarning("Empty $dateType date $dateStr")
            return null
        } else {
            try {
                return xlsDateFromString(dateStr)
            } catch(e: DateTimeParseException) {
                reportWarning("Illegal $dateType date $dateStr, format is ${Config.datePattern}")
                return null
            }
        }
    }

    fun hasFather(): Boolean =
            arrayOf(fatherName, fatherPhone).any { !StringUtils.isEmpty(it) }

    fun hasMother(): Boolean =
            arrayOf(motherName, motherPhone).any { !StringUtils.isEmpty(it) }

    fun hasAunt(): Boolean =
            arrayOf(auntName, auntPhone).any { !StringUtils.isEmpty(it) }

    fun hasGrandma(): Boolean =
            arrayOf(grandmaName, grandmaPhone).any { !StringUtils.isEmpty(it) }

    fun hasCard1(): Boolean =
            arrayOf(card1PurchaseDate, card1ExpirationDate, card1SoldLessons, card1FreeLessons, card1Price).any { !StringUtils.isEmpty(it) }

    fun hasCard2(): Boolean =
            arrayOf(card2PurchaseDate, card2ExpirationDate, card2SoldLessons, card2FreeLessons, card2Price).any { !StringUtils.isEmpty(it) }

    fun hasCard3(): Boolean =
            arrayOf(card3PurchaseDate, card3ExpirationDate, card3SoldLessons, card3FreeLessons, card3Price).any { !StringUtils.isEmpty(it) }

    private fun sqlDate(dateStr: String?): String? {
        try {
            return if (StringUtils.isEmpty(dateStr)) null else sqlDateFormat.format(xlsDateFormat.parse(dateStr))
        } catch (e: ParseException) {
            reportWarning("Invalid date $dateStr")
            return null
        }

    }

    private fun str(value: String?): String? =
            if (StringUtils.isEmpty(value)) null else "'${value!!.replace("'", "''")}'"


    private fun gender(value: String): Int {
        var gender = Gender.boy
        try {
            if (!StringUtils.isEmpty(value)) {
                gender = Gender.valueOf(value.toLowerCase())
            }
        } catch (ignored: IllegalArgumentException) {
            reportWarning("Invalid gender $value")
        }
        return gender.ordinal
    }

    private fun normalizeName(name: String): String =
            if (name.matches(Regex("[\\w\\s]+")))
                name.split(Regex("\\s+")).map { it.toLowerCase().capitalize() }.joinToString(separator = " ")
            else
                name

    private fun registerPhone(phone: String, businessId: String) {
        var businessIds = mobilePhones[phone]
        if (businessIds == null) {
            businessIds = mutableListOf()
            mobilePhones[phone] = businessIds
        }
        businessIds.add(businessId)
    }

    fun registerPhones() {
        if (!StringUtils.isEmpty(fatherPhone)) {
            registerPhone(fatherPhone, regularBusinessId)
        }
        if (!StringUtils.isEmpty(motherPhone)) {
            registerPhone(motherPhone, regularBusinessId)
        }
        if (!StringUtils.isEmpty(auntPhone)) {
            registerPhone(auntPhone, regularBusinessId)
        }
        if (!StringUtils.isEmpty(grandmaPhone)) {
            registerPhone(grandmaPhone, regularBusinessId)
        }
    }

    fun aggregateLessonsPeriod(soldLessons: String, freeLessons: String, expirationDate: String, purchaseDate: String) {
        val lessonsLimit = soldLessons.toInt(0) + freeLessons.toInt(0)
        if (!expirationDate.isEmpty() && !purchaseDate.isEmpty() && lessonsLimit > 0) {
            val durationDays = getCardDuration(expirationDate, purchaseDate)
            aggregator.add(durationDays.toDouble() / lessonsLimit)
        }
    }

    fun aggregateLessonsPeriods() {
        aggregateLessonsPeriod(card1SoldLessons, card1FreeLessons, card1ExpirationDate, card1PurchaseDate)
        aggregateLessonsPeriod(card2SoldLessons, card2FreeLessons, card2ExpirationDate, card2PurchaseDate)
        aggregateLessonsPeriod(card3SoldLessons, card3FreeLessons, card3ExpirationDate, card3PurchaseDate)
    }

    fun toSqlInsertStatement(): String {
        val hibernateSeq = KidCenterXlsSqlStudentsCardsLessonsConverter.hibernateSeq!!
        val studentCardSeq = KidCenterXlsSqlStudentsCardsLessonsConverter.studentCardSeq
        val studentId = if (toCreateInDb) hibernateSeq.getAndInc() else getStudentId()
        val managerId = managerIds[rowNumber % managerIds.size]
        val fatherId = if (hasFather()) hibernateSeq.getAndInc() else null
        val motherId = if (hasMother()) hibernateSeq.getAndInc() else null
        val auntId = if (hasAunt()) hibernateSeq.getAndInc() else null
        val grandmaId = if (hasGrandma()) hibernateSeq.getAndInc() else null

        val regularCard1Id = if (hasCard1() && !StringUtils.isEmpty(card1SoldLessons)) studentCardSeq!!.getAndInc() else null
        val bonusCard1Id = if (hasCard1() && !StringUtils.isEmpty(card1FreeLessons)) studentCardSeq!!.getAndInc() else null
        val regularCard2Id = if (hasCard2() && !StringUtils.isEmpty(card2SoldLessons)) studentCardSeq!!.getAndInc() else null
        val bonusCard2Id = if (hasCard2() && !StringUtils.isEmpty(card2FreeLessons)) studentCardSeq!!.getAndInc() else null
        val regularCard3Id = if (hasCard3() && !StringUtils.isEmpty(card3SoldLessons)) studentCardSeq!!.getAndInc() else null
        val bonusCard3Id = if (hasCard3() && !StringUtils.isEmpty(card3FreeLessons)) studentCardSeq!!.getAndInc() else null

        val sqlBuilder: StringBuilder = StringBuilder()
        if (toCreateInDb) {
            sqlBuilder.append("insert into public.student (id, businessid, trialbusinessid, nameen, namecn, birthdate, gender, status, manager_id) ")
                    .append("values ($studentId, ${str(regularBusinessId)}, ${str(trialBusinessId)}, ${str(normalizeName(nameEn))}, ${str(nameCn)}, ${str(sqlDate(birthDay))}, ${gender(gender)}, 4, $managerId);\n")
            tryAppendRelative(sqlBuilder, studentId, fatherId, "父亲", fatherName, fatherPhone)
            tryAppendRelative(sqlBuilder, studentId, motherId, "母亲", motherName, motherPhone)
            tryAppendRelative(sqlBuilder, studentId, auntId, "阿姨", auntName, auntPhone)
            tryAppendRelative(sqlBuilder, studentId, grandmaId, "奶奶", grandmaName, grandmaPhone)
        } else {
            sqlBuilder.append("update public.student set businessid=${str(regularBusinessId)}, nameen=${str(normalizeName(nameEn))}, namecn=${str(nameCn)}, birthdate=${str(sqlDate(birthDay))}, gender=${gender(gender)}, status=4, manager_id=$managerId where id=$studentId;\n")
        }

        val visitsMap = getVisitsMap()
        if (visitsMap.size() > card1SoldLessons.toInt(0) + card1FreeLessons.toInt(0) + card2SoldLessons.toInt(0) + card2FreeLessons.toInt(0) + card3SoldLessons.toInt(0) + card3FreeLessons.toInt(0)) {
            reportWarning("Total number of visited lessons in month columns is more than number of lessons in card")
        }

        tryAppendCardsPair(sqlBuilder, bonusCard1Id, regularCard1Id, studentId, card1PurchaseDate, card1ExpirationDate, card1SoldLessons, card1FreeLessons, card1Price, visitsMap)
        tryAppendCardsPair(sqlBuilder, bonusCard2Id, regularCard2Id, studentId, card2PurchaseDate, card2ExpirationDate, card2SoldLessons, card2FreeLessons, card2Price, visitsMap)
        tryAppendCardsPair(sqlBuilder, bonusCard3Id, regularCard3Id, studentId, card3PurchaseDate, card3ExpirationDate, card3SoldLessons, card3FreeLessons, card3Price, visitsMap)

        sqlBuilder.append("\n")

        return sqlBuilder.toString()
    }

    private fun getStudentId(): Long = existingRegularBusinessIdsToIds[regularBusinessId] ?: existingTrialBusinessIdsToIds[trialBusinessId]!!

    private fun tryAppendCardsPair(sqlBuilder: StringBuilder, bonusCardId: Long?, regularCardId: Long?, studentId: Long, purchaseDate: String, expirationDate: String, soldLessons: String, freeLessons: String, price: String, visitsMap: VisitsMap) {
        val durationDays = getCardDuration(expirationDate, purchaseDate)
        val soldLessonsNumeric = soldLessons.toInt(0)
        val freeLessonsNumeric = freeLessons.toInt(0)

        if (soldLessonsNumeric > 0) {
            tryAppendStudentCard(sqlBuilder, regularCardId, studentId, VisitType.regular, purchaseDate, durationDays, soldLessonsNumeric, soldLessonsNumeric, price.toInt(0))
        }
        if (freeLessonsNumeric > 0) {
            tryAppendStudentCard(sqlBuilder, bonusCardId, studentId, VisitType.bonus, purchaseDate, durationDays, freeLessonsNumeric, freeLessonsNumeric, 0)
        }

        tryAppendLessonVisits(sqlBuilder, visitsMap, studentId, regularCardId, purchaseDate, soldLessonsNumeric, VisitType.regular)
        tryAppendLessonVisits(sqlBuilder, visitsMap, studentId, bonusCardId, purchaseDate, freeLessonsNumeric, VisitType.regular)
    }

    private fun tryAppendLessonVisits(sqlBuilder: StringBuilder, visitsMap: VisitsMap, studentId: Long, cardId: Long?, purchaseDate: String, lessonsCount: Int, visitType: VisitType) {
        if (cardId != null && !visitsMap.isEmpty()) {
            var activationDate: LocalDate? = null
            var planDate = DateTimeUtils.max(xlsDateFromString(purchaseDate), visitsMap.firstVisitDate()!!)
            var runningLessonsCount = lessonsCount
            while (runningLessonsCount > 0 && !visitsMap.isEmpty()) {

                val planDay = LessonDay.fromWeekDay(planDate.dayOfWeek)
                templateLessonSlots.forEach { slot ->

                    if (planDay == slot.day && runningLessonsCount > 0 && visitsMap.getVisitCountAtMonthOf(planDate) > 0) {
                        val lessonId = LessonSlotId.fromTemplateSlot(slot, planDate)

                        if (!visitedLessonIds.contains(lessonId)) {
                            var bookedLessonsCount = bookedLessons[lessonId]
                            if (bookedLessonsCount == null) {
                                bookedLessonsCount = 0
                                appendLessonSlot(sqlBuilder, slot, lessonId)
                            }
                            if (bookedLessonsCount < Config.maxSlotsHardLimit) {
                                appendStudentSlot(sqlBuilder, lessonId, cardId, studentId, visitType)
                                visitsMap.removeVisitAtMonthOf(planDate)
                                runningLessonsCount--
                                bookedLessonsCount++
                                bookedLessons[lessonId] = bookedLessonsCount
                                visitedLessonIds.add(lessonId)
                                if (activationDate == null) {
                                    activationDate = planDate
                                }
                            }
                        }
                    }
                }

                val visitInMonthRemaining = visitsMap.getVisitCountAtMonthOf(planDate)
                if (visitInMonthRemaining > 0) {
                    planDate = planDate.plusDays(1)
                } else if (!visitsMap.isEmpty()) {
                    planDate = visitsMap.firstVisitDate()!!
                }
            }
            if (runningLessonsCount < lessonsCount) {
                sqlBuilder.append("update public.studentcard set lessonsavailable=$runningLessonsCount, activationdate=${str(activationDate!!.toSqlDate().toString())} where id=$cardId;\n")
            }
        }
    }

    private fun appendLessonSlot(sqlBuilder: StringBuilder, slot: TemplateLessonSlot, lessonId: LessonSlotId) {
        sqlBuilder.append("insert into public.lessonslot (id, datetime, subject, status, agegroup) ")
                .append("values (${str(lessonId.id())}, ${str(lessonId.dateTime.toSqlTimestamp().toString())}, ${lessonId.subject.ordinal}, ${LessonSlotStatus.closed.ordinal}, ${slot.ageGroup.ordinal});\n")
    }

    private fun appendStudentSlot(sqlBuilder: StringBuilder, lessonId: LessonSlotId, cardId: Long, studentId: Long, visitType: VisitType) {
        sqlBuilder.append("insert into public.studentslot (id, status, visittype, card_id, lessonslot_id, student_id, repeatsleft) ")
                .append("values (${hibernateSeq!!.getAndInc()}, ${StudentSlotStatus.visited.ordinal}, ${visitType.ordinal}, $cardId, ${str(lessonId.id())}, $studentId, 1);\n")
    }

    private fun getCardDuration(expirationDate: String, purchaseDate: String): Int {
        if (expirationDate.isEmpty() || purchaseDate.isEmpty()) return 730
        val purchaseDateTime = xlsDateFromString(purchaseDate).toLocalDateTimeMidnight()
        val expirationDateTime = xlsDateFromString(expirationDate).toLocalDateTimeMidnight()
        return Duration.between(purchaseDateTime, expirationDateTime).toDays().toInt()
    }

    private fun xlsDateFromString(date: String) = DateTimeUtils.dateFromString(date, xlsDatePattern)

    private fun tryAppendRelative(sqlBuilder: StringBuilder,
                                  studentId: Long,
                                  relativeId: Long?,
                                  role: String,
                                  name: String,
                                  phone: String) {
        if (relativeId != null) {
            sqlBuilder.append("insert into public.studentrelative (id, role, name, mobile) ")
                    .append("values ($relativeId, ${str(role)}, ${str(normalizeName(name))}, ${str(phone)});\n")
                    .append("insert into public.student_studentrelative (student_id, relatives_id) ")
                    .append("values ($studentId, $relativeId);\n")
        }
    }

    private fun tryAppendStudentCard(sqlBuilder: StringBuilder,
                                     id: Long?,
                                     studentId: Long,
                                     visitType: VisitType,
                                     purchaseDate: String,
                                     durationDays: Int,
                                     lessonsLimit: Int,
                                     lessonsAvailable: Int,
                                     price: Int) {
        if (id != null) {
            sqlBuilder.append("insert into public.studentcard (id, student_id, visitType, activationDate, purchaseDate, durationDays, lessonsLimit, lessonsAvailable, price, cancelslimit, cancelsavailable, late_cancels_limit, late_cancels_available, last_moment_cancels_limit, last_moment_cancels_available, undue_cancels_limit, undue_cancels_available, miss_limit, miss_available, suspendslimit, suspendsavailable) ")
                    .append("values ($id, $studentId, ${visitType.ordinal}, ${str(sqlDate(purchaseDate))}, ${str(sqlDate(purchaseDate))}, $durationDays, $lessonsLimit, $lessonsAvailable, $price, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50);\n")

        }
    }

    private fun getVisitsMap(): VisitsMap {
        return VisitsMap(mapOf(
                Pair(LocalDate.of(2015, 10, 1), v2015_10.toInt(0)),
                Pair(LocalDate.of(2015, 11, 1), v2015_11.toInt(0)),
                Pair(LocalDate.of(2015, 12, 1), v2015_12.toInt(0)),
                Pair(LocalDate.of(2016, 1, 1), v2016_01.toInt(0)),
                Pair(LocalDate.of(2016, 2, 1), v2016_02.toInt(0)),
                Pair(LocalDate.of(2016, 3, 1), v2016_03.toInt(0)),
                Pair(LocalDate.of(2016, 4, 1), v2016_04.toInt(0)),
                Pair(LocalDate.of(2016, 5, 1), v2016_05.toInt(0)),
                Pair(LocalDate.of(2016, 6, 1), v2016_06.toInt(0)),
                Pair(LocalDate.of(2016, 7, 1), v2016_07.toInt(0)),
                Pair(LocalDate.of(2016, 8, 1), v2016_08.toInt(0)),
                Pair(LocalDate.of(2016, 9, 1), v2016_09.toInt(0)),
                Pair(LocalDate.of(2016, 10, 1), v2016_10.toInt(0)),
                Pair(LocalDate.of(2016, 11, 1), v2016_11.toInt(0)),
                Pair(LocalDate.of(2016, 12, 1), v2016_12.toInt(0)),
                Pair(LocalDate.of(2017, 1, 1), v2017_01.toInt(0)),
                Pair(LocalDate.of(2017, 2, 1), v2017_02.toInt(0)),
                Pair(LocalDate.of(2017, 3, 1), v2017_03.toInt(0)),
                Pair(LocalDate.of(2017, 4, 1), v2017_04.toInt(0)),
                Pair(LocalDate.of(2017, 5, 1), v2017_05.toInt(0)),
                Pair(LocalDate.of(2017, 6, 1), v2017_06.toInt(0)),
                Pair(LocalDate.of(2017, 7, 1), v2017_07.toInt(0))
        ))
    }


    private fun reportWarning(message: String) {
        println("Warn row: $rowNumber, regularBusinessId: $regularBusinessId, $message")
    }

    override fun toString(): String {
        return "KcstlImportStudentRow(rowNumber=$rowNumber, toCreateInDb=$toCreateInDb, regularBusinessId='$regularBusinessId', trialBusinessId='$trialBusinessId', nameEn='$nameEn', nameCn='$nameCn', birthDay='$birthDay', gender='$gender', fatherName='$fatherName', fatherPhone='$fatherPhone', motherName='$motherName', motherPhone='$motherPhone', auntName='$auntName', auntPhone='$auntPhone', grandmaName='$grandmaName', grandmaPhone='$grandmaPhone', card1PurchaseDate='$card1PurchaseDate', card1ExpirationDate='$card1ExpirationDate', card1SoldLessons='$card1SoldLessons', card1FreeLessons='$card1FreeLessons', card1Price='$card1Price', card2PurchaseDate='$card2PurchaseDate', card2ExpirationDate='$card2ExpirationDate', card2SoldLessons='$card2SoldLessons', card2FreeLessons='$card2FreeLessons', card2Price='$card2Price', card3PurchaseDate='$card3PurchaseDate', card3ExpirationDate='$card3ExpirationDate', card3SoldLessons='$card3SoldLessons', card3FreeLessons='$card3FreeLessons', card3Price='$card3Price', v2015_10='$v2015_10', v2015_11='$v2015_11', v2015_12='$v2015_12', v2016_01='$v2016_01', v2016_02='$v2016_02', v2016_03='$v2016_03', v2016_04='$v2016_04', v2016_05='$v2016_05', v2016_06='$v2016_06', v2016_07='$v2016_07', v2016_08='$v2016_08', v2016_09='$v2016_09', v2016_10='$v2016_10', v2016_11='$v2016_11', v2016_12='$v2016_12', v2017_01='$v2017_01', v2017_02='$v2017_02', v2017_03='$v2017_03', v2017_04='$v2017_04', v2017_05='$v2017_05', v2017_06='$v2017_06', v2017_07='$v2017_07', xlsDatePattern='$xlsDatePattern', xlsDateFormat=$xlsDateFormat, sqlDateFormat=$sqlDateFormat, regularBusinessIdPattern='$regularBusinessIdPattern', trialBusinessIdPattern='$trialBusinessIdPattern', mobilePattern='$mobilePattern', visitedLessonIds=$visitedLessonIds)"
    }
}

class VisitsMap(visitsMap: Map<LocalDate, Int>) {
    private val map: SortedMap<LocalDate, Int> = visitsMap.filter { it.value > 0 }.toSortedMap()

    fun isEmpty() = map.isEmpty()

    fun size() = map.values.sum()

    fun getVisitCountAtMonthOf(date: LocalDate): Int {
        val refDate = date.withDayOfMonth(1)
        return map[refDate] ?: 0
    }

    fun removeVisitAtMonthOf(date: LocalDate): Boolean {
        val refDate = date.withDayOfMonth(1)
        val visitsCount = map[refDate]
        if (visitsCount != null && visitsCount > 1) {
            map[refDate] = visitsCount - 1
            return true
        } else {
            return map.remove(refDate) != null
        }
    }

    fun firstVisitDate(): LocalDate? = map.firstKey()
}

class JdbcConnectionManager {

    fun doWithConnection(action: (Connection) -> Unit) {
        Class.forName("org.postgresql.Driver")
        val settings = loadConnectionSettings()
        DriverManager.getConnection(settings.jdbcUrl, settings.user, settings.pass).use { action.invoke(it) }
    }

    private fun loadConnectionSettings(): JdbcConnectionSetings {
        val settings = loadConnectionSettingsFromResource("application-default.properties")
        if (settings == null) {
            return loadConnectionSettingsFromResource("application.properties")!!
        }
        return settings
    }

    private fun loadConnectionSettingsFromResource(resourceName: String): JdbcConnectionSetings? {
        val properties = loadProperties(resourceName)
        if (properties != null) {
            val jdbcUrl = properties.get("spring.datasource.url") as String
            val user = properties.get("spring.datasource.username") as String
            val pass = properties.get("spring.datasource.password") as String
            return JdbcConnectionSetings(jdbcUrl, user, pass)
        }
        return null
    }

    private fun loadProperties(resourcePath: String): Properties? {
        val stream = this.javaClass.classLoader.getResourceAsStream(resourcePath)
        stream.use {
            if (stream == null) {
                return null
            } else {
                val properties = Properties()
                properties.load(stream)
                return properties
            }
        }
    }
}


class JdbcConnectionSetings(val jdbcUrl: String, val user: String, val pass: String)