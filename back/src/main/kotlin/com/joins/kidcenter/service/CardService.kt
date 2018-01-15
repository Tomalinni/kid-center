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

import com.joins.kidcenter.domain.AgeRange
import com.joins.kidcenter.domain.Card
import com.joins.kidcenter.domain.VisitType
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.CardRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.ValidatorsUtil
import com.joins.kidcenter.utils.ensure
import com.joins.kidcenter.utils.trueClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface CardService : FindOneService<Card>, SaveService<Card, SaveResult<Card>>, DeleteOneService {
    fun findAll(searchRequest: CardSearchRequest): SearchResult<Card>
}

@Service
@Transactional
open class CardServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: CardRepository
) : EntityService(em, queryBuilder), CardService {


    @Transactional(readOnly = true)
    override fun findOne(id: Long): Card? = repository.findOne(id)

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: CardSearchRequest): SearchResult<Card> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1
            val listQuery = listQuery(searchRequest)
            val countQuery = countQuery(searchRequest)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = em.createQuery(countQuery)
                    .singleResult as Long

            @Suppress("UNCHECKED_CAST")
            return SearchResult((results as Iterable<Card>).toList(), total)
        } else {
            return SearchResult(listOf<Card>(), 0)
        }

    }

    private fun listQuery(data: CardSearchRequest): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Card::class.java)
        val orderFieldId: String = data.sortColumn.ensure(listOf("price", "lessonsLimit"), "id")
        val orderField = from.get<Any>(orderFieldId)
        val order = if (orderFieldId == "id") cb.desc(orderField) else data.sortOrder.selectOrder(cb, orderField)
        return criteria.select(from).where(getPredicate(data, from)).orderBy(order)
    }

    private fun countQuery(data: CardSearchRequest): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Card::class.java)
        return criteria.select(cb.count(from)).where(getPredicate(data, from))
    }

    private fun getPredicate(data: CardSearchRequest, from: Root<out Any>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val visitTypePredicate = getVisitTypePredicate(cb, data, from)
        val ageRangePredicate = getAgeRangePredicate(cb, data, from)
        val activeStatePredicate = getActiveStatePredicate(data, from)

        return cb.and(visitTypePredicate, ageRangePredicate, activeStatePredicate)
    }

    private fun getVisitTypePredicate(cb: CriteriaBuilder, data: CardSearchRequest, from: Root<out Any>): Predicate? {
        val visitType = data.visitType
        return if (visitType == null) cb.trueClause() else {
            val predicate = cb.equal(from.get<VisitType>("visitType"), visitType.option)
            return if (visitType.negate) cb.not(predicate) else predicate
        }
    }

    private fun getAgeRangePredicate(cb: CriteriaBuilder, data: CardSearchRequest, from: Root<out Any>): Predicate? {
        val ageRange = data.ageRange
        return if (ageRange == null)
            cb.trueClause()
        else
            cb.equal(from.get<AgeRange>("ageRange"), ageRange.option)
    }

    private fun getActiveStatePredicate(data: CardSearchRequest, from: Root<out Any>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        return when (data.activeState) {
            CardActiveState.all -> {
                cb.trueClause()
            }
            CardActiveState.active -> {
                cb.and(
                        cb.equal(from.get<Boolean>("active"), true),
                        cb.greaterThanOrEqualTo(from.get<LocalDate>("expirationDate"), DateTimeUtils.currentDate()),
                        cb.lessThanOrEqualTo(from.get<LocalDate>("creationDate"), DateTimeUtils.currentDate())
                )
            }
            CardActiveState.inactive -> {
                cb.or(
                        cb.equal(from.get<Boolean>("active"), false),
                        cb.lessThan(from.get<LocalDate>("expirationDate"), DateTimeUtils.currentDate()),
                        cb.greaterThan(from.get<LocalDate>("creationDate"), DateTimeUtils.currentDate())
                )
            }
        }
    }

    override fun save(entity: Card): SaveResult<Card> {
        val result: SaveResult<Card> = ValidatorsUtil.validateCard(entity)
        if (result.hasErrors()) {
            return result
        }
        return SaveResult(repository.save(entity))
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}
