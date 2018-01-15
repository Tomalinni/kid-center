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

package com.joins.kidcenter.service.providers

import com.joins.kidcenter.domain.Account
import com.joins.kidcenter.domain.Payment
import com.joins.kidcenter.domain.School
import com.joins.kidcenter.dto.PaymentDto
import com.joins.kidcenter.dto.PaymentOriginRequestPart
import com.joins.kidcenter.dto.PaymentSearchRequest
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.AccountRepository
import com.joins.kidcenter.repository.PaymentRepository
import com.joins.kidcenter.repository.SchoolRepository
import com.joins.kidcenter.service.*
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigInteger
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

@Component
open class PaymentDataProvider @Autowired constructor(
        val em: EntityManager,
        val queryBuilder: EntityListQueryBuilder,
        val repository: PaymentRepository,
        val accountRepository: AccountRepository,
        val schoolRepository: SchoolRepository,
        val categoriesHelper: CategoriesHelper
) {

    fun findByFilters(searchRequest: PaymentSearchRequest, ascByDate: Boolean = false): SearchResult<PaymentDto> {
        val sourceAccountIds = getAccountIds(searchRequest.source)
        val targetAccountIds = getAccountIds(searchRequest.target)
        val anyEndPointAccountIds = getAccountIds(searchRequest.anyEndpoint)

        val categoryIds = searchRequest.getEffectiveCategoryIds(categoriesHelper.getCategoryMap())
        val startDate = searchRequest.getEffectiveStartDate()
        val endDate = searchRequest.getEffectiveEndDate()

        if (searchRequest.isRecordNumbersValid() &&
                (sourceAccountIds == null || !sourceAccountIds.isEmpty()) &&
                (targetAccountIds == null || !targetAccountIds.isEmpty()) &&
                (anyEndPointAccountIds == null || !anyEndPointAccountIds.isEmpty()) &&
                (categoryIds == null || !categoryIds.isEmpty()) &&
                !startDate.isAfter(endDate)) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1
            val predicateFactory = FiltersPredicateFactory(em, categoriesHelper, searchRequest, sourceAccountIds, targetAccountIds, anyEndPointAccountIds)
            val listQuery = listQuery(predicateFactory, OrderFactoryImpl(em, ascByDate))
            val countQuery = countQuery(predicateFactory)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = em.createQuery(countQuery)
                    .singleResult as Long

            @Suppress("UNCHECKED_CAST")
            val dtos = (results as Iterable<Payment>).toList().map { PaymentDto.fromDomainObject(it) }
            mergeWithStudentBusinessIds(dtos)
            return SearchResult(dtos, total)
        } else {
            return SearchResult(listOf<PaymentDto>(), 0)
        }
    }

    fun findByText(searchRequest: PaymentSearchRequest, ascByDate: Boolean = false): SearchResult<PaymentDto> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1
            val predicateFactory = TextPredicateFactory(em, categoriesHelper, searchRequest)
            val listQuery = listQuery(predicateFactory, OrderFactoryImpl(em, ascByDate))
            val countQuery = countQuery(predicateFactory)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = em.createQuery(countQuery)
                    .singleResult as Long

            @Suppress("UNCHECKED_CAST")
            val dtos = (results as Iterable<Payment>).toList().map { PaymentDto.fromDomainObject(it) }
            mergeWithStudentBusinessIds(dtos)
            return SearchResult(dtos, total)
        } else {
            return SearchResult(listOf<PaymentDto>(), 0)
        }
    }


    private fun mergeWithStudentBusinessIds(dtos: List<PaymentDto>) {
        if (dtos.isNotEmpty()) {
            val idToDto = dtos.map { Pair(it.id, it) }.toMap()
            repository.findStudentBusinessIds(idToDto.keys).forEach {
                val paymentId = (it[0] as BigInteger).toLong()
                val paymentDto = idToDto[paymentId]
                if (paymentDto != null) {
                    paymentDto.studentBusinessId = it[1] as String?
                }
            }
        }
    }

    private fun listQuery(predicateFactory: ListPredicateFactory<Payment>, orderFactory: OrderFactory<Payment>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Payment::class.java)
        return criteria.select(from).where(predicateFactory.createPredicate(from)).orderBy(*orderFactory.create(from))
    }

    private fun countQuery(predicateFactory: ListPredicateFactory<Payment>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Payment::class.java)
        return criteria.select(cb.count(from)).where(predicateFactory.createPredicate(from))
    }

    fun getAccountIds(origin: PaymentOriginRequestPart): Collection<Long>? {
        val accountIds = origin.accountIds
        @Suppress("FoldInitializerAndIfToElvis")
        if (accountIds == null) {
            if (origin.schoolId == null) {
                return null
            } else {
                return accountRepository.findIdsBySchoolId(origin.schoolId!!)
            }
        }
        return accountIds
    }

    fun getSchoolIds(origin: PaymentOriginRequestPart): Collection<Long> {
        val schoolId = origin.schoolId
        @Suppress("FoldInitializerAndIfToElvis")
        if (schoolId == null) {
            if (origin.accountIds == null || origin.accountIds!!.isEmpty()) {
                return listOf()
            } else {
                return schoolRepository.findIdsByAccountIds(origin.accountIds!!)
            }
        }
        return listOf(schoolId)
    }

    fun getSchoolMap(): Map<Long, School> {
        return schoolRepository.findAll().map { Pair(it.id!!, it) }.toMap()
    }

    fun getAccountMap(): Map<Long, Account> {
        return accountRepository.findAll().map { Pair(it.id!!, it) }.toMap()
    }
}