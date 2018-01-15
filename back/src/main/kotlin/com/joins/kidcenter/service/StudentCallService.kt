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

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.StudentCallRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.ValidatorsUtil
import com.joins.kidcenter.utils.toLocalDateTimeLastDayMoment
import com.joins.kidcenter.utils.toLocalDateTimeMidnight
import com.joins.kidcenter.utils.trueClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface StudentCallService : FindOneService<StudentCall>, SaveService<StudentCall, SaveResult<StudentCall>>, DeleteOneService {
    fun findAll(searchRequest: StudentCallSearchRequest): SearchResult<StudentCallListDto>
}

@Service
@Transactional
open class StudentCallServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: StudentCallRepository
) : EntityService(em, queryBuilder), StudentCallService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): StudentCall? = repository.findOne(id)

    override fun findAll(searchRequest: StudentCallSearchRequest): SearchResult<StudentCallListDto> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1

            val predicateFactory = StudentCallFiltersPredicateFactory(em, searchRequest)
            val listQuery = listQuery(predicateFactory)
            val countQuery = countQuery(predicateFactory)

            @Suppress("UNCHECKED_CAST")
            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList as List<StudentCall>

            val total = if (results.size == maxResults || firstResult > 0)
                em.createQuery(countQuery).singleResult as Long
            else
                results.size.toLong()

            @Suppress("UNCHECKED_CAST")
            return SearchResult(results.map { StudentCallListDto.fromDomainObject(it) }, total)
        } else {
            return SearchResult(listOf<StudentCallListDto>(), 0)
        }
    }

    private fun listQuery(predicateFactory: ListPredicateFactory<StudentCall>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(StudentCall::class.java)
        return criteria.select(from).where(predicateFactory.createPredicate(from)).orderBy(cb.asc(from.get<Any>("id")))
    }

    private fun countQuery(predicateFactory: ListPredicateFactory<StudentCall>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(StudentCall::class.java)
        return criteria.select(cb.count(from)).where(predicateFactory.createPredicate(from))
    }

    override fun save(entity: StudentCall): SaveResult<StudentCall> {
        val validation = ValidatorsUtil.validateStudentCall(entity)
        if (validation.hasErrors()) {
            return validation
        }
        return SaveResult(repository.save(entity))
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}

class StudentCallFiltersPredicateFactory(
        val em: EntityManager,
        val data: StudentCallSearchRequest) : ListPredicateFactory<StudentCall> {

    override fun createPredicate(from: Root<StudentCall>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val startDate = data.getEffectiveStartDate()
        val endDate = data.getEffectiveEndDate()

        val studentPredicate = if (data.studentId == null) cb.trueClause() else cb.equal(from.get<Student>("student").get<Long>("id"), data.studentId)
        val datePredicate = cb.and(cb.greaterThanOrEqualTo(from.get("date"), startDate.toLocalDateTimeMidnight()), cb.lessThanOrEqualTo(from.get("date"), endDate.toLocalDateTimeLastDayMoment()))
        val studentStatusPredicate = if (data.studentStatus == null) cb.trueClause() else cb.equal(from.get<Student>("student").get<StudentStatus>("status"), data.studentStatus)
        val methodPredicate = if (data.method == null) cb.trueClause() else cb.equal(from.get<StudentCallMethod>("method"), data.method)
        val resultPredicate = if (data.result == null) cb.trueClause() else cb.equal(from.get<StudentCallResult>("result"), data.result)
        return cb.and(studentPredicate, datePredicate, studentStatusPredicate, methodPredicate, resultPredicate)
    }
}