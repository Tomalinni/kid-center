package com.joins.kidcenter.utils.imports

import com.github.excelmapper.core.engine.*
import com.github.excelmapper.core.engine.CellDefinitions.fromReferences
import com.github.excelmapper.core.engine.References.property
import org.apache.commons.lang3.StringUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*

class KidCenterXlsSqlKinderGardensConverter {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            KidCenterXlsSqlKinderGardensConverter().convert()
        }
    }

    val importFilePath = "data/kinder-gardens.xlsx"
    val sqlFilePath = "data/kinder-gardens.sql"
    val importedColumns = arrayOf("name", "address", "phone")
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
            val container = factory.createItemContainer(sheet, CellCoordinate(1, 2))

            output.write("delete from public.kindergarden;\n")

            var sequenceId = 1
            var row = readItem(container)
            while (!row.isEmpty()) {
                val insertedRows = row.toSqlInsertStatement(sequenceId.toLong())
                sequenceId += insertedRows.rowsCount
                output.write(insertedRows.sql)

                row = readItem(container)
            }
            output.write("create sequence kinder_garden_seq;\n")
            output.write("select setval('public.kinder_garden_seq', ${sequenceId - 1});\n")
        } finally {
            if (input != null) {
                input.close()
            }
            if (output != null) {
                output.close()
            }
        }
    }

    private fun readItem(container: ItemContainer): ImportKinderGarden {
        val row = ImportKinderGarden()
        container.readItem(row, group, SimpleProcessMessagesHolder())
        return row.trim()
    }
}

class ImportKinderGarden(var name: String = "",
                         var address: String = "",
                         var phone: String = "") {

    fun trim(): ImportKinderGarden =
            ImportKinderGarden(
                    StringUtils.trimToEmpty(name),
                    StringUtils.trimToEmpty(address),
                    StringUtils.trimToEmpty(phone)
            )

    fun isEmpty(): Boolean =
            arrayOf(name, address, phone)
                    .all { StringUtils.isEmpty(it) }

    private fun str(value: String): String? =
            if (StringUtils.isEmpty(value)) null else "'${value.replace("'", "''")}'"


    override fun toString(): String =
            "ImportKinderGarden(name='$name', address='$address', phone='$phone')"

    fun toSqlInsertStatement(id: Long): InsertedRows {
        val sqlBuilder = StringBuilder("insert into public.kindergarden (id, name, address, phone) ")
                .append("values ($id, ${str(name)}, ${str(address)}, ${str(phone)});\n")

        sqlBuilder.append("\n")

        return InsertedRows(sqlBuilder.toString(), 1)
    }

}
