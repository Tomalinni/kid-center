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

package com.joins.kidcenter.service.export

import com.github.excelmapper.core.engine.*
import com.github.excelmapper.core.utils.CellUtils
import com.joins.kidcenter.controller.PaymentPhotoFields
import com.joins.kidcenter.domain.Account
import com.joins.kidcenter.domain.School
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.PaymentRepository
import com.joins.kidcenter.repository.SchoolRepository
import com.joins.kidcenter.service.providers.PaymentDataProvider
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.service.storage.StorageProviderImpl
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.ExcelUtils.addPicture
import com.joins.kidcenter.utils.ExcelUtils.getCell
import com.joins.kidcenter.utils.ExcelUtils.insertPicture
import com.joins.kidcenter.utils.ExcelUtils.setCellSizeInPx
import com.joins.kidcenter.utils.PhotoNames
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.math.BigInteger


interface PaymentExportService {
    fun export(searchRequest: PaymentSearchRequest, out: OutputStream)
}

@Service
@Transactional
open class PaymentExportServiceImpl @Autowired constructor(
        val paymentDataProvider: PaymentDataProvider,
        val paymentRepository: PaymentRepository,
        val storageService: FileStorageServiceImpl,
        val schoolRepository: SchoolRepository
) : PaymentExportService {

    val factory = ItemContainerFactory()
    val maxPhotoDimensionPx = 100
    val extensionToPictureType: Map<String, Int> = mapOf(
            Pair("jpg", Workbook.PICTURE_TYPE_JPEG),
            Pair("jpeg", Workbook.PICTURE_TYPE_JPEG),
            Pair("png", Workbook.PICTURE_TYPE_PNG)
    )

    override fun export(searchRequest: PaymentSearchRequest, out: OutputStream) {
        val addedPictures: MutableMap<String, Int> = mutableMapOf()
        val wb = XSSFWorkbook()

        val schoolMap = paymentDataProvider.getSchoolMap()
        val schoolIdToAccountId = getSchoolIdToAccountId()
        val schoolIds = getEffectiveSchoolIds(searchRequest)

        val sheet = wb.createSheet("Total")
        val container = factory.createItemContainer(sheet)

        addBalanceTable(wb, container, searchRequest, schoolIdToAccountId)
        container.nextRow()
        val sumBalance = addPaymentsTable(SchoolExportContext.global(), container, searchRequest, addedPictures)
        addSumBalance(container, sumBalance, wb)

        autoSizeColumns(cellGroup(wb, ExportPaymentDto.globalTableColIds), sheet)

        schoolIds.forEach { schoolId ->
            val singleSchoolSearchRequest = extractSingleSchoolRequest(searchRequest, schoolId)
            val pageSheet = wb.createSheet(schoolMap[schoolId]?.name ?: "School $schoolId")
            val pageContainer = factory.createItemContainer(pageSheet)

            addBalanceTable(wb, pageContainer, singleSchoolSearchRequest, schoolIdToAccountId, true)
            pageContainer.nextRow()
            addPaymentsTable(SchoolExportContext.school(schoolId), pageContainer, singleSchoolSearchRequest, addedPictures)
            autoSizeColumns(cellGroup(wb, ExportPaymentDto.schoolRelatedColIds), pageSheet)
        }

        wb.write(out)
        out.flush()
        addedPictures.clear()
    }

    private fun addSumBalance(container: ItemContainer, sumBalance: ExportBalanceSumDto, wb: XSSFWorkbook) {
        container.plusColumn(ExportPaymentDto.globalTableColIds.indexOf("price"))
        writeItem(sumBalance, balanceSumCellGroup(wb), container)
    }

    private fun getSchoolIdToAccountId(): Map<Long, List<Long>> {
        val result: MutableMap<Long, MutableList<Long>> = mutableMapOf()
        schoolRepository.findSchoolAccountIdPairs().
                forEach {
                    val schoolId = (it[0] as BigInteger).toLong()
                    val accountId = (it[1] as BigInteger).toLong()
                    var accountIds: MutableList<Long>? = result[schoolId]
                    if (accountIds == null) {
                        accountIds = mutableListOf()
                        result[schoolId] = accountIds
                    }
                    accountIds.add(accountId)
                }
        return result
    }

    private fun getEffectiveSchoolIds(searchRequest: PaymentSearchRequest): Collection<Long> {
        var schoolIds = paymentDataProvider.getSchoolIds(originAccessor(searchRequest))
        if (schoolIds.isEmpty()) {
            schoolIds = schoolRepository.findIdsByExternal(false)
        }
        return schoolIds
    }

    private fun extractSingleSchoolRequest(searchRequest: PaymentSearchRequest, it: Long): PaymentSearchRequest {
        val singleSchoolSearchRequest = searchRequest.clone()
        originAccessor(singleSchoolSearchRequest).schoolId = it
        return singleSchoolSearchRequest
    }

    private fun addBalanceTable(wb: XSSFWorkbook, container: ItemContainer, searchRequest: PaymentSearchRequest, schoolIdToAccountId: Map<Long, List<Long>>, useSchoolRelatedCols: Boolean = false) {
        val titleCellGroup = titleCellGroup(wb, if (useSchoolRelatedCols) ExportAccountPeriodSumDto.accBalanceTitles else ExportAccountPeriodSumDto.schoolAccBalanceTitles)
        val cellGroup = cellGroup(wb, if (useSchoolRelatedCols) ExportAccountPeriodSumDto.accBalanceIds else ExportAccountPeriodSumDto.schoolAccBalanceIds)

        container.addItem(null, titleCellGroup)
        val requestSchoolId = originAccessor(searchRequest).schoolId
        val schoolIds = if (requestSchoolId == null) schoolRepository.findIdsByExternal(false) else listOf(requestSchoolId)
        val accountIds = schoolIds.map { schoolIdToAccountId[it] }.flatMap { it ?: listOf() }.toSet()

        val expenses = if (accountIds.isEmpty()) listOf() else paymentRepository.findExpenseByPeriod(accountIds, schoolIds, searchRequest.getEffectiveStartDate(), searchRequest.getEffectiveEndDate())
                .map(mapToAccountSchoolSum())
        val incomes = if (accountIds.isEmpty()) listOf() else paymentRepository.findIncomeByPeriod(accountIds, schoolIds, searchRequest.getEffectiveStartDate(), searchRequest.getEffectiveEndDate())
                .map(mapToAccountSchoolSum())
        val balances: MutableMap<Pair<Long, Long>, AccountSchoolBalance> = mutableMapOf()
        expenses.forEach {
            var expBalance = balances[Pair(it.schoolId, it.accountId)]
            if (expBalance == null) {
                expBalance = AccountSchoolBalance(it.schoolId, it.accountId)
                balances[Pair(it.schoolId, it.accountId)] = expBalance
            }
            expBalance.expense = it.sum
        }

        incomes.forEach {
            var incBalance = balances[Pair(it.schoolId, it.accountId)]
            if (incBalance == null) {
                incBalance = AccountSchoolBalance(it.schoolId, it.accountId)
                balances[Pair(it.schoolId, it.accountId)] = incBalance
            }
            incBalance.income = it.sum
        }

        val totalSchoolExpense = expenses.sumByDouble { it.sum.toDouble() }
        val totalSchoolIncome = incomes.sumByDouble { it.sum.toDouble() }

        addTotalSumResult(container, totalSchoolExpense, totalSchoolIncome, cellGroup)
        balances.values.forEach {
            addAccountSumResult(container, it, cellGroup)
        }

        if (!useSchoolRelatedCols) {
            val balancesBySchool: MutableMap<Long, AccountSchoolBalance> = mutableMapOf()
            val balancesByAccount: MutableMap<Long, AccountSchoolBalance> = mutableMapOf()
            balances.values.forEach {
                balancesBySchool[it.schoolId] = (balancesBySchool[it.schoolId] ?: AccountSchoolBalance.schoolZero(it.schoolId)).plus(it)
                balancesByAccount[it.accountId] = (balancesByAccount[it.accountId] ?: AccountSchoolBalance.accountZero(it.accountId)).plus(it)
            }

            val accountMap: Map<Long, Account> = paymentDataProvider.getAccountMap()
            val schoolMap: Map<Long, School> = paymentDataProvider.getSchoolMap()


            container.currentCoordinate = CellCoordinate(5, 0)
            container.addItem(null, titleCellGroup(wb, ExportAccountPeriodSumDto.schoolBalanceTitles))
            balancesBySchool.forEach {
                val schoolName = schoolMap[it.value.schoolId]?.name ?: ""
                writeItem(ExportAccountPeriodSumDto(schoolName, "", it.value.expense.toDouble(), it.value.income.toDouble()), cellGroup(wb, ExportAccountPeriodSumDto.schoolBalanceIds), container)
                container.nextRow()
            }

            container.currentCoordinate = CellCoordinate(9, 0)
            container.addItem(null, titleCellGroup(wb, ExportAccountPeriodSumDto.accBalanceTitles))
            balancesByAccount.forEach {
                val accNumber = AccountDto.getName(accountMap[it.value.accountId])
                writeItem(ExportAccountPeriodSumDto("", accNumber, it.value.expense.toDouble(), it.value.income.toDouble()), cellGroup(wb, ExportAccountPeriodSumDto.accBalanceIds), container)
                container.nextRow()
            }

            container.currentCoordinate = CellCoordinate(0, balances.size + 2)
        }
    }

    private fun mapToAccountSchoolSum(): (Array<Any>) -> AccountSchoolSum = { AccountSchoolSum((it[1] as BigInteger).toLong(), (it[0] as BigInteger).toLong(), it[2] as BigDecimal) }

    private fun addPaymentsTable(exportContext: SchoolExportContext, container: ItemContainer, singleSchoolSearchRequest: PaymentSearchRequest, addedPictures: MutableMap<String, Int>): ExportBalanceSumDto {
        val wb = container.sheet.workbook
        val titleCellGroup = titleCellGroup(wb, if (exportContext.isGlobal()) ExportPaymentDto.globalTableColTitles else ExportPaymentDto.schoolRelatedColTitles)
        val colIds = if (exportContext.isGlobal()) ExportPaymentDto.globalTableColIds else ExportPaymentDto.schoolRelatedColIds

        container.addItem(null, titleCellGroup)
        return addPayments(exportContext, container, singleSchoolSearchRequest, colIds, addedPictures)
    }

    private fun originAccessor(searchRequest: PaymentSearchRequest): PaymentOriginRequestPart =
            when (searchRequest.direction) {
                PaymentDirection.outgoing -> searchRequest.source
                PaymentDirection.incoming -> searchRequest.target
                PaymentDirection.transfer, null -> searchRequest.anyEndpoint
            }


    private fun addPayments(exportContext: SchoolExportContext, container: ItemContainer, searchRequest: PaymentSearchRequest, colIds: Array<String>, addedPictures: MutableMap<String, Int>): ExportBalanceSumDto {
        val wb = container.sheet.workbook
        val expenseCellGroup = cellGroup(wb, colIds, paymentRowStyleReference(wb, ExportPaymentDto.expenseCellsColor))
        val incomeCellGroup = cellGroup(wb, colIds, paymentRowStyleReference(wb, ExportPaymentDto.incomeCellsColor))
        val transferCellGroup = cellGroup(wb, colIds, paymentRowStyleReference(wb, ExportPaymentDto.transferCellsColor))

        searchRequest.pageRecordsCount = 500
        var runExpenseTotal: Double = 0.0
        var runIncomeTotal: Double = 0.0
        var runTotal: Double = 0.0
        var nextRecord = 1
        var total = 0L
        var totalSet = false

        while (!totalSet || nextRecord <= total) {
            searchRequest.firstRecord = nextRecord
            nextRecord = searchRequest.firstRecord + searchRequest.pageRecordsCount
            val result = paymentDataProvider.findByFilters(searchRequest, true)

            if (!totalSet) {
                total = result.total
                totalSet = true
            }

            result.results.map {
                if (exportContext.isIncomingPayment(it)) {
                    runIncomeTotal += it.price
                    runTotal += it.price
                } else if (exportContext.isOutgoingPayment(it)) {
                    runExpenseTotal += it.price
                    runTotal -= it.price
                } else if (exportContext.isTransferPayment(it)) {
                    runIncomeTotal += it.price
                    runExpenseTotal += it.price
                }


                ExportPaymentDto.fromPaymentDto(it, runTotal, exportContext)
            }.forEach {
                if (exportContext.isIncomingPayment(it)) {
                    writeItem(it, incomeCellGroup, container)
                } else if (exportContext.isOutgoingPayment(it)) {
                    writeItem(it, expenseCellGroup, container)
                } else if (exportContext.isTransferPayment(it)) {
                    writeItem(it, transferCellGroup, container)
                }

                if (!searchRequest.skipPhotos) {
                    addPhotos(it.id, expenseCellGroup, container, addedPictures)
                }
                container.nextRow()
            }
        }

        return ExportBalanceSumDto(runExpenseTotal, runIncomeTotal, runIncomeTotal - runExpenseTotal)
    }

    private fun <T> writeItem(item: T, cellGroup: CellGroup, container: ItemContainer) {
        for ((coordinateInGroup, cellDefinition) in cellGroup.entries) {
            val sheetCellCoordinate = container.currentCoordinate.plusCoordinate(coordinateInGroup)
            val valueRef = cellDefinition.valueRef

            @Suppress("UNCHECKED_CAST", "UsePropertyAccessSyntax")
            (valueRef as ContextAware<T>).setContext(item)
            val propertyValue = valueRef.value
            CellUtils.setCellStyle(container.sheet, sheetCellCoordinate, cellGroup.cellStyleReference.cellStyle)
            val cell = getCell(container.sheet, sheetCellCoordinate, true)
            if (propertyValue is Number) {
                val doubleVal = Math.round(propertyValue.toDouble() * 100).toDouble() / 100
                cell?.setCellValue(doubleVal)
            } else {
                cell?.setCellValue(propertyValue?.toString())
            }

            setCellSpans(container, sheetCellCoordinate, cellDefinition.rowSpan, cellDefinition.colSpan)
        }
    }

    private fun setCellSpans(container: ItemContainer, cellCoordinate: CellCoordinate, rowSpan: Int, colSpan: Int) {
        if (rowSpan != 1 || colSpan != 1) {
            CellUtils.setCellSpans(container.sheet, cellCoordinate, rowSpan, colSpan)
        }
    }

    private fun addPhotos(paymentId: Long, cellGroup: CellGroup, container: ItemContainer, addedPictures: MutableMap<String, Int>) {
        val productPhotoProvider = storageService.providers().payment(paymentId, PaymentPhotoFields.productPhotos.toString())
        val receiptPhotoProvider = storageService.providers().payment(paymentId, PaymentPhotoFields.receiptPhotos.toString())

        val photosAdded = addPhotosGroup(paymentId, container, productPhotoProvider, cellGroup.columnCount, addedPictures)
        addPhotosGroup(paymentId, container, receiptPhotoProvider, cellGroup.columnCount + photosAdded, addedPictures)
    }

    private fun addPhotosGroup(paymentId: Long, container: ItemContainer, photoProvider: StorageProviderImpl, curColumnShift: Int, addedPictures: MutableMap<String, Int>): Int {
        var photosAdded: Int = 0

        photoProvider.listNames().forEach { name ->
            val pictureIndex = tryAddAndGetPictureIndex(paymentId, container, name, photoProvider, addedPictures)
            if (pictureIndex != null) {
                val photoCoordinate = container.currentCoordinate.plusColumn(curColumnShift + photosAdded + 1)
                setCellSizeInPx(container.sheet, photoCoordinate, maxPhotoDimensionPx, maxPhotoDimensionPx)
                insertPicture(container.sheet, photoCoordinate, pictureIndex, maxPhotoDimensionPx, maxPhotoDimensionPx)
                photosAdded++
            }
        }
        return photosAdded
    }

    private fun tryAddAndGetPictureIndex(paymentId: Long, container: ItemContainer, name: String, photoProvider: StorageProviderImpl, addedPictures: MutableMap<String, Int>): Int? {
        val pictureIndex = addedPictures["$paymentId:$name"]
        if (pictureIndex != null) return pictureIndex

        val pictureDescription: Pair<Int, ByteArray>? = getPictureDescription(name, photoProvider)
        if (pictureDescription != null) {
            val pictIndex = addPicture(container.sheet, pictureDescription.second, pictureDescription.first)
            addedPictures["$paymentId:$name"] = pictIndex
            return pictIndex
        }
        return null
    }

    private fun getPictureDescription(name: String, photoProvider: StorageProviderImpl): Pair<Int, ByteArray>? {
        val extension: String = PhotoNames.getFileNameExtension(name).toLowerCase()
        val pictureType: Int? = extensionToPictureType[extension]
        if (pictureType != null) {
            val fileSize: Long = photoProvider.size(name)

            if (fileSize != -1L) {
                val fileStream = ByteArrayOutputStream(fileSize.toInt())
                photoProvider.download(name, fileStream)
                val description = Pair(pictureType, fileStream.toByteArray())

                return description
            }
        }
        return null
    }

    private fun addTotalSumResult(container: ItemContainer, expense: Double, income: Double, cellGroup: CellGroup) {
        writeItem(ExportAccountPeriodSumDto("", "Sum", expense, income), cellGroup, container)
        container.nextRow()
    }

    private fun addAccountSumResult(container: ItemContainer, balance: AccountSchoolBalance, cellGroup: CellGroup) {
        val accountMap: Map<Long, Account> = paymentDataProvider.getAccountMap()
        val schoolMap: Map<Long, School> = paymentDataProvider.getSchoolMap()
        val accNumber = AccountDto.getName(accountMap[balance.accountId])
        val schoolName = schoolMap[balance.schoolId]?.name ?: ""
        writeItem(ExportAccountPeriodSumDto(schoolName, accNumber, balance.expense.toDouble(), balance.income.toDouble()), cellGroup, container)
        container.nextRow()
    }

    private fun autoSizeColumns(rowCellGroup: CellGroup, sheet: XSSFSheet) {
        val startColumn = rowCellGroup.topLeftCorner!!.column
        val endColumn = rowCellGroup.topRightCorner!!.column
        for (i in startColumn..endColumn) {
            sheet.autoSizeColumn(i)
        }
    }
}

@Suppress("unused")
class ExportPaymentDto(
        val id: Long,
        val date: String,
        val cat: String,
        val cat2: String,
        val cat3: String,
        val cat4: String,
        val cat5: String,
        val direction: PaymentDirection,
        val price: Double?,
        val incomingPrice: Double?,
        val runTotal: Double?,
        val comment: String,
        val productUrl: String,
        val accNumber: String,
        val targetAccNumber: String,
        val schoolId: Long,
        val schoolName: String,
        val targetSchoolId: Long,
        val targetSchoolName: String
) {
    companion object {
        val schoolRelatedColIds = arrayOf("date", "cat", "cat2", "cat3", "cat4", "cat5", "price", "incomingPrice", "runTotal", "comment", "accNumber", "targetAccNumber", "productUrl")
        val globalTableColIds = arrayOf("date", "cat", "cat2", "cat3", "cat4", "cat5", "price", "incomingPrice", "runTotal", "comment", "accNumber", "schoolName", "targetAccNumber", "targetSchoolName", "productUrl")
        val schoolRelatedColTitles = arrayOf("日期", "种类", "附一", "附二", "附三", "附四", "金额", "金额", "余额", "注明", "帐号", "目标帐号", "链接")
        val globalTableColTitles = arrayOf("日期", "种类", "附一", "附二", "附三", "附四", "金额", "金额", "余额", "注明", "帐号", "中心", "目标帐号", "目标中心", "链接")
        val expenseCellsColor = Color(91, 155, 213)
        val incomeCellsColor = Color(112, 173, 71)
        val transferCellsColor = Color(200, 200, 200)

        fun fromPaymentDto(dto: PaymentDto, runTotal: Double, exportContext: SchoolExportContext): ExportPaymentDto {
            return ExportPaymentDto(
                    dto.id,
                    DateTimeUtils.dateToString(dto.date),
                    dto.category?.name ?: "",
                    dto.category2?.name ?: "",
                    dto.category3?.name ?: "",
                    dto.category4?.name ?: "",
                    dto.category5?.name ?: "",
                    dto.direction,
                    if (exportContext.isOutgoingPayment(dto) || exportContext.isTransferPayment(dto)) dto.price else null,
                    if (exportContext.isIncomingPayment(dto) || exportContext.isTransferPayment(dto)) dto.price else null,
                    runTotal,
                    dto.comment,
                    dto.productUrl,
                    dto.account?.name ?: "",
                    dto.targetAccount?.name ?: "",
                    dto.school?.id ?: 0L,
                    dto.school?.name ?: "",
                    dto.targetSchool?.id ?: 0L,
                    dto.targetSchool?.name ?: ""
            )
        }
    }
}

class SchoolExportContext private constructor(val schoolId: Long?) {

    fun isGlobal() = schoolId == null

    fun isIncomingPayment(payment: PaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.incoming
        else
            payment.targetSchool?.id == schoolId && payment.school?.id != schoolId
    }

    fun isIncomingPayment(payment: ExportPaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.incoming
        else
            payment.targetSchoolId == schoolId && payment.schoolId != schoolId
    }

    fun isOutgoingPayment(payment: PaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.outgoing
        else
            payment.school?.id == schoolId && payment.targetSchool?.id != schoolId
    }

    fun isOutgoingPayment(payment: ExportPaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.outgoing
        else
            payment.schoolId == schoolId && payment.targetSchoolId != schoolId
    }

    fun isTransferPayment(payment: PaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.transfer
        else
            payment.school?.id == schoolId && payment.targetSchool?.id == schoolId
    }

    fun isTransferPayment(payment: ExportPaymentDto): Boolean {
        return if (isGlobal())
            payment.direction == PaymentDirection.transfer
        else
            payment.schoolId == schoolId && payment.targetSchoolId == schoolId
    }

    companion object {
        fun school(schoolId: Long?) = SchoolExportContext(schoolId)
        fun global() = SchoolExportContext(null)
    }
}

@Suppress("unused")
class ExportAccountPeriodSumDto(val schoolName: String,
                                val accNumber: String,
                                val expense: Double,
                                val income: Double) {
    companion object {
        val accBalanceIds = arrayOf("accNumber", "expense", "income")
        val schoolBalanceIds = arrayOf("schoolName", "expense", "income")
        val schoolAccBalanceIds = arrayOf("schoolName", "accNumber", "expense", "income")
        val accBalanceTitles = arrayOf("账号", "expense", "income")
        val schoolBalanceTitles = arrayOf("中心", "expense", "income")
        val schoolAccBalanceTitles = arrayOf("中心", "账号", "expense", "income")
    }
}

@Suppress("unused")
class ExportBalanceSumDto(val expense: Double,
                          val income: Double,
                          val profit: Double) {
    companion object {
        val ids = arrayOf("expense", "income", "profit")
    }
}

fun cellGroup(wb: Workbook, colIds: Array<String>, styleReference: CellStyleReference = rowStyleReference(wb)): CellGroup {
    return CellGroup().apply { this.cellStyleReference = styleReference }
            .addCells(CellDefinitions(colIds.map { CellDefinition(References.property(it)) }))
}

fun titleCellGroup(wb: Workbook, colTitles: Array<String>): CellGroup {
    return CellGroup().apply { cellStyleReference = titleStyleReference(wb) }
            .addCells(CellDefinitions(colTitles.map { CellDefinition(References.value(it)) }))
}

fun balanceSumCellGroup(wb: Workbook): CellGroup {
    return CellGroup().apply { this.cellStyleReference = balanceSumStyleReference(wb) }
            .addCell(CellCoordinate.ZERO, References.property("expense"))
            .addCell(CellCoordinate(1, 0), References.property("income"))
            .addCell(CellCoordinate(0, 1), CellDefinition(References.property("profit"), 2, 1))
}

fun titleStyleReference(wb: Workbook): StaticCellStyleReference {
    val titleStyle = wb.createCellStyle()
    val font = wb.createFont()
    font.fontHeightInPoints = 18
    titleStyle.setFont(font)
    val styleReference = StaticCellStyleReference(titleStyle)
    return styleReference
}

fun rowStyleReference(wb: Workbook): StaticCellStyleReference {
    val cellStyle = wb.createCellStyle()
    val font = wb.createFont()
    font.fontHeightInPoints = 12
    cellStyle.setFont(font)
    val styleReference = StaticCellStyleReference(cellStyle)
    return styleReference
}

fun paymentRowStyleReference(wb: Workbook, color: Color): StaticCellStyleReference {
    val cellStyle = wb.createCellStyle()
    val font = wb.createFont()
    font.fontHeightInPoints = 12
    cellStyle.setFont(font)
    cellStyle.fillPattern = CellStyle.SOLID_FOREGROUND
    (cellStyle as XSSFCellStyle).setFillForegroundColor(XSSFColor(color))
    val styleReference = StaticCellStyleReference(cellStyle)
    return styleReference
}

fun balanceSumStyleReference(wb: Workbook): StaticCellStyleReference {
    val cellStyle = wb.createCellStyle()
    val font = wb.createFont()
    font.fontHeightInPoints = 16
    cellStyle.setFont(font)
    cellStyle.fillPattern = CellStyle.SOLID_FOREGROUND
    (cellStyle as XSSFCellStyle).setFillForegroundColor(XSSFColor(Color(255, 0, 0)))
    val styleReference = StaticCellStyleReference(cellStyle)
    return styleReference
}
