package com.joins.kidcenter.service

import com.joins.kidcenter.domain.Homework
import com.joins.kidcenter.domain.LessonAgeGroup
import com.joins.kidcenter.domain.LessonSlot
import com.joins.kidcenter.domain.LessonSubject
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.HomeworkRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.ValidatorsUtil
import com.joins.kidcenter.utils.trueClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface HomeworkService : FindOneService<HomeworkDto>, SaveService<HomeworkDto, SaveResult<HomeworkDto>>, DeleteOneService {
    fun findAll(searchRequest: HomeworkSearchRequest): SearchResult<HomeworkDto>
    fun findByLesson(lesson: LessonSlot): HomeworkDto?
}

@Service
@Transactional
open class HomeworkServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: HomeworkRepository
) : EntityService(em, queryBuilder), HomeworkService {
    override fun findAll(searchRequest: HomeworkSearchRequest): SearchResult<HomeworkDto> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1
            val predicateFactory = HomeworkFiltersPredicateFactory(em, searchRequest)
            val listQuery = listQuery(predicateFactory)
            val countQuery = countQuery(predicateFactory)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = if (results.size == maxResults || firstResult > 0)
                em.createQuery(countQuery).singleResult as Long
            else
                results.size.toLong()

            @Suppress("UNCHECKED_CAST")
            return SearchResult((results as Iterable<Homework>).toList().map { HomeworkDto.fromDomainObject(it) }, total)

        } else {
            return SearchResult(listOf<HomeworkDto>(), 0)
        }
    }

    override fun findOne(id: Long): HomeworkDto? {
        val homework = repository.findOne(id)
        return if (homework == null) null else HomeworkDto.fromDomainObject(homework)
    }

    override fun save(entity: HomeworkDto): SaveResult<HomeworkDto> {

        val result: SaveResult<HomeworkDto> = ValidatorsUtil.validateHomework(entity)
        if (result.hasErrors()) {
            return result
        }

        val homework: Homework = Homework().apply {
            id = entity.id
            subject = entity.subject
            ageGroup = entity.ageGroup
            startDate = entity.startDate
            endDate = entity.endDate
        }
        val saveResult: Homework = repository.save(homework)
        entity.apply { id = saveResult.id!! }
        return SaveResult(entity)
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }

    override fun findByLesson(lesson: LessonSlot): HomeworkDto? {
        val params = mapOf("subject" to lesson.subject.name,
                "ageGroup" to lesson.ageGroup.name,
                "dateInsidePeriod" to lesson.dateTime.toString())

        val predicateFactory = HomeworkFiltersPredicateFactory(em, HomeworkSearchRequest.fromMap(params))
        val listQuery = listQuery(predicateFactory)
        val result = em.createQuery(listQuery)
                .setMaxResults(1)
                .resultList
        @Suppress("UNCHECKED_CAST")
        return if (result.size > 0) HomeworkDto.fromDomainObject((result as Iterable<Homework>).toList()[0]) else null
    }

    private fun listQuery(predicateFactory: ListPredicateFactory<Homework>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Homework::class.java)
        return criteria.select(from).where(predicateFactory.createPredicate(from)).orderBy(cb.asc(from.get<Any>("id")))
    }

    private fun countQuery(predicateFactory: ListPredicateFactory<Homework>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Homework::class.java)
        return criteria.select(cb.count(from)).where(predicateFactory.createPredicate(from))
    }


    class HomeworkFiltersPredicateFactory(
            val em: EntityManager,
            val data: HomeworkSearchRequest) : ListPredicateFactory<Homework> {

        override fun createPredicate(from: Root<Homework>): Predicate {
            val cb: CriteriaBuilder = em.criteriaBuilder
            val startDate = data.getEffectiveStartDate()
            val endDate = data.getEffectiveEndDate()
            val dateInsidePeriod = data.dateInsidePeriod
            val datePredicate: Predicate?
            if (dateInsidePeriod != null) {
                datePredicate = cb.and(cb.lessThanOrEqualTo(from.get("startDate"), dateInsidePeriod), cb.greaterThanOrEqualTo(from.get("endDate"), dateInsidePeriod))
            } else {
                datePredicate = cb.and(cb.greaterThanOrEqualTo(from.get("startDate"), startDate), cb.lessThanOrEqualTo(from.get("startDate"), endDate))
            }
            val lessonSubjectPredicate = if (data.subject == null) cb.trueClause() else cb.equal(from.get<LessonSubject>("subject"), data.subject)
            val studentAgePredicate = if (data.ageGroup == null) cb.trueClause() else cb.equal(from.get<LessonAgeGroup>("ageGroup"), data.ageGroup)

            return cb.and(datePredicate, lessonSubjectPredicate, studentAgePredicate)
        }
    }
}