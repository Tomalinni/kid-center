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

package com.joins.kidcenter.service

import com.joins.kidcenter.controller.PaymentPhotoFields
import com.joins.kidcenter.domain.Account
import com.joins.kidcenter.domain.Category
import com.joins.kidcenter.domain.Payment
import com.joins.kidcenter.domain.School
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.CategoryRepository
import com.joins.kidcenter.repository.PaymentRepository
import com.joins.kidcenter.service.maps.AccountSchoolMonthStorage
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.service.providers.PaymentDataProvider
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.criteria.*

interface PaymentService : FindOneService<PaymentDto>, SaveService<Payment, SaveResult<Payment>>, DeleteOneService {
    fun findAll(searchRequest: PaymentSearchRequest): SearchResult<PaymentDto>

    fun findStat(searchRequest: PaymentSearchRequest): LineChartResult

    fun findBalance(request: PaymentBalanceRequest): BalanceResult

    fun updatePhotosCount(paymentId: Long, fieldId: String, photosCount: Int)
}

@Service
@Transactional
open class PaymentServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: PaymentRepository,
        val paymentDataProvider: PaymentDataProvider,
        val categoryRepository: CategoryRepository,
        val accountMonthStorage: AccountSchoolMonthStorage,
        val storageService: FileStorageServiceImpl,
        val categoriesHelper: CategoriesHelper
) : EntityService(em, queryBuilder),
        PaymentService {


    @Transactional(readOnly = true)
    override fun findOne(id: Long): PaymentDto? {
        val payment = repository.findOne(id)
        return if (payment == null) null else PaymentDto.fromDomainObject(payment).apply {
            receiptPhotos = storageService.providers().payment(payment.id!!, PaymentPhotoFields.receiptPhotos.toString()).listNames()
            productPhotos = storageService.providers().payment(payment.id!!, PaymentPhotoFields.productPhotos.toString()).listNames()
        }
    }

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: PaymentSearchRequest): SearchResult<PaymentDto> {
        return when (searchRequest.searchMethod) {
            PaymentSearchMethod.filters -> paymentDataProvider.findByFilters(searchRequest)
            PaymentSearchMethod.text -> paymentDataProvider.findByText(searchRequest)
        }
    }

    private fun getStatCategoryPredicate(data: PaymentSearchRequest, from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val categoriesMap = categoriesHelper.getCategoryMap()
        var categoryIds = data.getEffectiveCategoryIds(categoriesMap)
        if (categoryIds == null) {
            categoryIds = categoriesMap.filter { it.value.parent == null }.map { it.value.id!! }
        }
        if (categoryIds.isEmpty()) {
            return cb.falseClause()
        } else {
            return categoriesHelper.getDefinedCategoriesPredicate(data, from, categoryIds)
        }
    }

    @Transactional(readOnly = true)
    override fun findStat(searchRequest: PaymentSearchRequest): LineChartResult {
        val range = getEffectiveStatRange(searchRequest)
        if (!range.startDate.isBefore(range.endDate) || searchRequest.searchMethod == PaymentSearchMethod.text) {
            return LineChartResult()
        }

        val accountIds = paymentDataProvider.getAccountIds(searchRequest.source)
        val targetAccountIds = paymentDataProvider.getAccountIds(searchRequest.target)
        if ((accountIds != null && accountIds.isEmpty()) ||
                (targetAccountIds != null && targetAccountIds.isEmpty())) {
            return LineChartResult()
        }

        val categoriesMap: Map<Long, Category> = categoryRepository.findAll().map { Pair(it.id!!, it) }.toMap()
        val statMap: MutableMap<Long, MutableList<BigDecimal>> = mutableMapOf()


        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val root = criteria.from(Payment::class.java)
        val categoryGroupByCol = getCategoryFieldName(root, searchRequest.groupBy)
        val accountCol = root.get<Account>("account").get<Long>("id")
        val targetAccountCol = root.get<Account>("targetAccount").get<Long>("id")
        val schoolCol = root.get<School>("school").get<Long>("id")
        val targetSchoolCol = root.get<School>("targetSchool").get<Long>("id")
        val monthDateCol = root.get<java.sql.Date>("monthDate")
        val sumPriceCol = cb.sum(root.get<BigDecimal>("price"))
        val monthDateOrder = cb.asc(monthDateCol)
        val selectionList = if (categoryGroupByCol == null) listOf(monthDateCol, sumPriceCol) else listOf(categoryGroupByCol, monthDateCol, sumPriceCol)
        val groupList = if (categoryGroupByCol == null) listOf(monthDateCol) else listOf(categoryGroupByCol, monthDateCol)
        val orderList = if (categoryGroupByCol == null) listOf(monthDateOrder) else listOf(monthDateOrder, cb.asc(categoryGroupByCol))
        val categoryClause = getStatCategoryPredicate(searchRequest, root)
        val directionClause = if (searchRequest.direction == null) cb.trueClause() else cb.equal(root.get<PaymentDirection>("direction"), searchRequest.direction)
        val accountClause = if (accountIds == null) cb.trueClause() else accountCol.`in`(accountIds)
        val targetAccountClause = if (targetAccountIds == null) cb.trueClause() else targetAccountCol.`in`(targetAccountIds)
        val schoolClause = if (searchRequest.source.schoolId == null) cb.trueClause() else cb.equal(schoolCol, searchRequest.source.schoolId)
        val targetSchoolClause = if (searchRequest.target.schoolId == null) cb.trueClause() else cb.equal(targetSchoolCol, searchRequest.target.schoolId)
        val predicate = cb.and(directionClause, categoryClause, accountClause, targetAccountClause, schoolClause, targetSchoolClause, cb.between(monthDateCol, range.startDate.toSqlDate(), range.endDate.toSqlDate()))

        val query = criteria.multiselect(selectionList).where(predicate).groupBy(groupList).orderBy(orderList)

        val items: List<PaymentStatItem> = em.createQuery(query).resultList.map {
            @Suppress("UNCHECKED_CAST")
            val item = it as Array<Any>
            val category = if (categoryGroupByCol == null) -1 else (item[0] as Long?) ?: 0
            val monthDateIndex = if (categoryGroupByCol == null) 0 else 1
            val monthDate = (item[monthDateIndex] as LocalDate?) ?: LocalDate.MIN
            val sumIndex = if (categoryGroupByCol == null) 1 else 2
            val sum = item[sumIndex] as BigDecimal
            PaymentStatItem(category, monthDate, sum)
        }.filter { it.isValid() }

        items.forEach { it ->
            var statItem = statMap[it.category]
            if (statItem == null) {
                statItem = Array(12, { i -> BigDecimal.ZERO }).toMutableList()
                statMap[it.category] = statItem
            }
            statItem[it.monthDate.monthValue - 1] = it.sum
        }

        val chartSeries = statMap.map {
            LineChartSerie().apply {
                val categoryId = it.key
                name = if (categoryId == -1L) "和" else categoriesMap[categoryId]?.name ?: ""
                data = it.value.toList()
            }
        }

        return LineChartResult().apply { series = chartSeries }

    }


    fun getCategoryFieldName(root: Root<Payment>, groupBy: StatGroupBy): Path<Long>? {
        return when (groupBy) {
            StatGroupBy.none -> null
            else -> root.get<Category>(groupBy.name).get<Long>("id")
        }
    }

    fun getEffectiveStatRange(searchRequest: PaymentSearchRequest): DateRange {
        val curDate = DateTimeUtils.currentDate()
        return when (searchRequest.period) {
            SearchPeriod.day, SearchPeriod.week, SearchPeriod.month, SearchPeriod.year, SearchPeriod.all -> DateRange(curDate.withDayOfYear(1), curDate.withDayOfYear(curDate.lengthOfYear()))
            SearchPeriod.custom -> DateRange(searchRequest.periodStart!!, searchRequest.periodEnd!!)
        }
    }

    override fun findBalance(request: PaymentBalanceRequest): BalanceResult {
        val accountIds = paymentDataProvider.getAccountIds(request)?.toSet() ?: setOf()
        val schoolIds = paymentDataProvider.getSchoolIds(request).toSet()
        return accountMonthStorage.get(request.getEffectiveBalanceRange(), accountIds, schoolIds)
    }

    override fun updatePhotosCount(paymentId: Long, fieldId: String, photosCount: Int) {
        val payment = repository.findOne(paymentId)
        if (payment != null) {
            when (fieldId) {
                "receiptPhotos" -> payment.receiptPhotosCount = photosCount
                "productPhotos" -> payment.productPhotosCount = photosCount
            }
        }
    }

    override fun save(entity: Payment): SaveResult<Payment> {
        val result: SaveResult<Payment> = ValidatorsUtil.validatePayment(entity)
        if (result.hasErrors()) {
            return result
        }

        var savedEntity: Payment? = null
        if (entity.id != null) {
            savedEntity = repository.findOne(entity.id)
            updatePhotosCount(entity, savedEntity)
        }
        updateMonthDate(entity)
        accountMonthStorage.applyPaymentChange(savedEntity, entity)
        @Suppress("UNCHECKED_CAST")
        val nextEntity = repository.save(entity)
        em.flush()
        return SaveResult(nextEntity)
    }

    private fun updatePhotosCount(entity: Payment, savedEntity: Payment?) {
        if (savedEntity != null) {
            entity.receiptPhotosCount = savedEntity.receiptPhotosCount
            entity.productPhotosCount = savedEntity.productPhotosCount
        }
    }

    private fun updateMonthDate(entity: Payment) {
        val category = entity.category
        if (category == null || !category.hasTargetMonth || entity.monthDate == null) {
            entity.monthDate = entity.date.withDayOfMonth(1)
        }
    }

    override fun delete(id: Long): DeleteResult<Long> {
        val payment = repository.findOne(id)
        if (payment != null) {
            @Suppress("UNCHECKED_CAST")
            repository.delete(payment)
            em.flush()
            accountMonthStorage.applyPaymentChange(payment, null)
        }
        return DeleteResult(id)
    }
}

@Component
open class CategoriesHelper @Autowired constructor(
        val em: EntityManager,
        val categoryRepository: CategoryRepository) {

    private val categoryCols = arrayOf("category", "category2", "category3", "category4", "category5")

    fun getDefinedCategoriesPredicate(data: PaymentSearchRequest, from: Root<Payment>, categoryIds: Collection<Long>): Predicate {
        return if (data.useInnerCategories) {
            anyCategoryInIdsPredicate(from, categoryIds)
        } else {
            rootCategoryInIdsPredicate(from, categoryIds)
        }
    }

    fun getCategoryIdsByNamePrefix(prefix: String): List<Long> {
        return categoryRepository.findIdsByNamePrefix(EscapeUtil.escapeLike(prefix.toLowerCase()))
    }

    fun getCategoryMap(): Map<Long, Category> {
        return categoryRepository.findAll().map { Pair(it.id!!, it) }.toMap()
    }

    fun anyCategoryInIdsPredicate(from: Root<Payment>, ids: Collection<Long>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        return if (ids.isEmpty()) cb.falseClause() else cb.or(*categoryCols
                .map { from.get<Category>(it).get<Long>("id").`in`(ids) }
                .toTypedArray())
    }

    fun rootCategoryInIdsPredicate(from: Root<Payment>, ids: Collection<Long>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        return if (ids.isEmpty()) cb.falseClause() else from.get<Category>("category").get<Long>("id").`in`(ids)
    }

}

interface ListPredicateFactory<T> {
    fun createPredicate(from: Root<T>): Predicate
}

class FiltersPredicateFactory(
        val em: EntityManager,
        val categoriesHelper: CategoriesHelper,
        val data: PaymentSearchRequest,
        val sourceAccountIds: Collection<Long>?,
        val targetAccountIds: Collection<Long>?,
        val anyEndpointAccountIds: Collection<Long>?) : ListPredicateFactory<Payment> {

    override fun createPredicate(from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val startDate = data.getEffectiveStartDate()
        val endDate = data.getEffectiveEndDate()

        val directionPredicate = if (data.direction == null) cb.trueClause() else cb.equal(from.get<PaymentDirection>("direction"), data.direction)

        val categoryPredicate = getListCategoryPredicate(data, from)
        val datePredicate = cb.and(cb.greaterThanOrEqualTo(from.get("date"), startDate), cb.lessThanOrEqualTo(from.get("date"), endDate))
        return cb.and(directionPredicate, schoolAccPredicate(from), categoryPredicate, datePredicate)
    }

    private fun schoolAccPredicate(from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder


        if (data.direction == null && data.anyEndpoint.schoolId != null) {
            val schoolPredicate = schoolIdPredicate(cb, from, "school", data.anyEndpoint.schoolId)
            val accountPredicate = if (anyEndpointAccountIds == null) cb.trueClause() else accountIdPredicate(from, "account", anyEndpointAccountIds)
            val targetSchoolPredicate = schoolIdPredicate(cb, from, "targetSchool", data.anyEndpoint.schoolId)
            val targetAccountPredicate = if (anyEndpointAccountIds == null) cb.trueClause() else accountIdPredicate(from, "targetAccount", anyEndpointAccountIds)
            return cb.or(cb.and(schoolPredicate, accountPredicate), cb.and(targetSchoolPredicate, targetAccountPredicate))

        } else {
            val schoolPredicate = if (data.source.schoolId == null) cb.trueClause() else schoolIdPredicate(cb, from, "school", data.source.schoolId)
            val accountPredicate = if (sourceAccountIds == null) cb.trueClause() else accountIdPredicate(from, "account", sourceAccountIds)
            val targetSchoolPredicate = if (data.target.schoolId == null) cb.trueClause() else schoolIdPredicate(cb, from, "targetSchool", data.target.schoolId)
            val targetAccountPredicate = if (targetAccountIds == null) cb.trueClause() else accountIdPredicate(from, "targetAccount", targetAccountIds)
            return cb.and(schoolPredicate, accountPredicate, targetSchoolPredicate, targetAccountPredicate)
        }
    }

    private fun schoolIdPredicate(cb: CriteriaBuilder, from: Root<Payment>, fieldId: String, schoolId: Long?) = cb.equal(from.get<School>(fieldId).get<Long>("id"), schoolId)

    private fun accountIdPredicate(from: Root<Payment>, fieldId: String, accountIds: Collection<Long>?) = from.get<Account>(fieldId).get<Long>("id").`in`(accountIds)

    private fun getListCategoryPredicate(data: PaymentSearchRequest, from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val categoryIds = data.getEffectiveCategoryIds(categoriesHelper.getCategoryMap())
        if (categoryIds == null) {
            return cb.trueClause()
        } else if (categoryIds.isEmpty()) {
            return cb.falseClause()
        } else {
            return categoriesHelper.getDefinedCategoriesPredicate(data, from, categoryIds)
        }
    }
}

class TextPredicateFactory(
        val em: EntityManager,
        val categoriesHelper: CategoriesHelper,
        val data: PaymentSearchRequest) : ListPredicateFactory<Payment> {

    override fun createPredicate(from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder

        val directionPredicate = if (data.direction == null) cb.trueClause() else cb.equal(from.get<PaymentDirection>("direction"), data.direction)
        return cb.and(directionPredicate, getPredicateFromText(from))
    }

    private fun getPredicateFromText(from: Root<Payment>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val text = data.text.trim()
        if (text.isEmpty()) return cb.trueClause()

        val searchPrice = text.toDouble(-1.0)
        if (searchPrice != -1.0) {
            val floorPrice = Math.floor(searchPrice)
            val ceilPrice = Math.ceil(searchPrice)
            return cb.between(from.get("price"), floorPrice, ceilPrice)
        } else {
            val categoryIds = categoriesHelper.getCategoryIdsByNamePrefix(text)
            return categoriesHelper.anyCategoryInIdsPredicate(from, categoryIds)
        }
    }
}

interface OrderFactory<T> {
    fun create(from: Root<T>): Array<Order>
}

class OrderFactoryImpl(
        val em: EntityManager,
        val ascByDate: Boolean) : OrderFactory<Payment> {

    override fun create(from: Root<Payment>): Array<Order> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val datCol = from.get<Any>("date")
        val dateOrder = if (ascByDate) cb.asc(datCol) else cb.desc(datCol)
        return arrayOf(dateOrder, cb.asc(from.get<Any>("id")))

    }
}
