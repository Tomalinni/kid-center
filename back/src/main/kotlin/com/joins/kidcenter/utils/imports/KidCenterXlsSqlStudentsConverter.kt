package com.joins.kidcenter.utils.imports

import com.github.excelmapper.core.engine.*
import com.github.excelmapper.core.engine.CellDefinitions.fromReferences
import com.github.excelmapper.core.engine.References.property
import com.joins.kidcenter.domain.Gender
import org.apache.commons.lang3.StringUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat

class KidCenterXlsSqlStudentsConverter() {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            KidCenterXlsSqlStudentsConverter().convert()
        }
    }

    val importFilePath = "data/kids.xlsx"
    val sqlFilePath = "data/kids.sql"
    val importedColumns = arrayOf("regularBusinessId", "nameEn", "nameCn", "birthDay", "gender", "_temp", "fatherName", "fatherPhone", "motherName", "motherPhone", "auntName", "auntPhone")
    val group: CellGroup = CellGroup().addCells(fromReferences(importedColumns.map { property(it) }))

    fun convert() {
        var input: InputStream? = null
        var output: Writer? = null
        try {
            input = FileInputStream(File(importFilePath))
            output = BufferedWriter(FileWriter(File(sqlFilePath)))
            val wb = XSSFWorkbook(input)
            val sheet = wb.getSheetAt(0)


            val factory = ItemContainerFactory()
            val container = factory.createItemContainer(sheet, CellCoordinate(2, 5))

            output.write("delete from public.student_studentrelative;\n")
            output.write("delete from public.studentrelative;\n")
            output.write("delete from public.student;\n")

            var sequenceId = 1
            var studentRow = readStudent(container)
            while (!studentRow.isEmpty()) {
                val insertedRows = studentRow.toSqlInsertStatement(sequenceId.toLong())
                sequenceId += insertedRows.rowsCount
                output.write(insertedRows.sql)

                studentRow = readStudent(container)
            }
            output.write("select setval('public.hibernate_sequence', ${sequenceId - 1});\n")
        } finally {
            if (input != null) {
                input.close()
            }
            if (output != null) {
                output.close()
            }
        }
    }

    private fun readStudent(container: ItemContainer): ImportStudentRow {
        val studentRow = ImportStudentRow()
        container.readItem(studentRow, group, SimpleProcessMessagesHolder())
        return studentRow.trim()
    }
}

class ImportStudentRow(var businessId: String = "",
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
                       var _temp: String = "") {

    private val birthDayFormat = SimpleDateFormat("dd.MM.yyyy")
    private val sqlBirthDayFormat = SimpleDateFormat("yyyy-MM-dd")

    fun trim(): ImportStudentRow =
            ImportStudentRow(
                    StringUtils.trimToEmpty(businessId),
                    StringUtils.trimToEmpty(nameEn),
                    StringUtils.trimToEmpty(nameCn),
                    StringUtils.trimToEmpty(birthDay),
                    StringUtils.trimToEmpty(gender),
                    StringUtils.trimToEmpty(fatherName),
                    StringUtils.trimToEmpty(fatherPhone),
                    StringUtils.trimToEmpty(motherName),
                    StringUtils.trimToEmpty(motherPhone),
                    StringUtils.trimToEmpty(auntName),
                    StringUtils.trimToEmpty(auntPhone)
            )

    fun isEmpty(): Boolean =
            arrayOf(businessId, nameEn, nameCn, birthDay, gender, fatherName, fatherPhone, motherName, motherPhone, auntName, auntPhone)
                    .all { StringUtils.isEmpty(it) }

    fun hasFather(): Boolean =
            arrayOf(fatherName, fatherPhone).any { !StringUtils.isEmpty(it) }

    fun hasMother(): Boolean =
            arrayOf(motherName, motherPhone).any { !StringUtils.isEmpty(it) }

    fun hasAunt(): Boolean =
            arrayOf(auntName, auntPhone).any { !StringUtils.isEmpty(it) }

    fun firstPhone(): String =
            if (hasFather()) fatherPhone
            else if (hasMother()) motherPhone
            else if (hasAunt()) auntPhone
            else ""

    private fun sqlBirthDay(dateStr: String): String {
        try {
            return if (StringUtils.isEmpty(dateStr)) "" else sqlBirthDayFormat.format(birthDayFormat.parse(dateStr))
        } catch (e: ParseException) {
            return ""
        }

    }

    private fun str(value: String): String? =
            if (StringUtils.isEmpty(value)) null else "'${value.replace("'", "''")}'"

    private fun gender(value: String): Int {
        var gender = Gender.boy
        try {
            if (!StringUtils.isEmpty(value)) {
                gender = Gender.valueOf(value.toLowerCase())
            }
        } catch (ignored: IllegalArgumentException) {
        }
        return gender.ordinal
    }

    private fun normalizeName(name: String): String =
            if (name.matches(Regex("[\\w\\s]+")))
                name.split(Regex("\\s+")).map { it.toLowerCase().capitalize() }.joinToString(separator = " ")
            else
                name


    override fun toString(): String =
            "ImportStudentRow(regularBusinessId='$businessId', nameEn='$nameEn', nameCn='$nameCn', birthDay='$birthDay', gender='$gender', fatherName='$fatherName', fatherPhone='$fatherPhone', motherName='$motherName', motherPhone='$motherPhone', auntName='$auntName', auntPhone='$auntPhone')"

    fun toSqlInsertStatement(firstId: Long): InsertedRows {
        var id = firstId;
        val studentId = id++
        val fatherId = if (hasFather()) id++ else null
        val motherId = if (hasMother()) id++ else null
        val auntId = if (hasAunt()) id++ else null
        val rowsCount = (id - firstId).toInt()
        val sqlBuilder = StringBuilder("insert into public.student (id, businessid, nameen, namecn, birthdate, gender, mobile, istrial) ")
                .append("values ($studentId, ${str(businessId)}, ${str(normalizeName(nameEn))}, ${str(nameCn)}, ${str(sqlBirthDay(birthDay))}, ${gender(gender)}, ${str(firstPhone())}, false);\n")

        tryAppendRelative(sqlBuilder, studentId, fatherId, "father", fatherName, fatherPhone)
        tryAppendRelative(sqlBuilder, studentId, motherId, "mother", motherName, motherPhone)
        tryAppendRelative(sqlBuilder, studentId, auntId, "aunt", auntName, auntPhone)
        sqlBuilder.append("\n")

        return InsertedRows(sqlBuilder.toString(), rowsCount)
    }

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


}

