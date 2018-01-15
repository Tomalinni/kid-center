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

package com.joins.kidcenter.service.lessons

import com.joins.kidcenter.domain.LessonSlot
import com.joins.kidcenter.domain.LessonTemplate
import com.joins.kidcenter.domain.StudentSlotStatus
import com.joins.kidcenter.domain.VisitType
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.dto.lessons.LessonTemplateInitDto
import com.joins.kidcenter.dto.lessons.LessonTemplateSaveDto
import com.joins.kidcenter.repository.LessonTemplateRepository
import com.joins.kidcenter.service.DeleteOneService
import com.joins.kidcenter.service.EntityService
import com.joins.kidcenter.service.FindOneService
import com.joins.kidcenter.service.SaveService
import com.joins.kidcenter.service.persistence.*
import com.joins.kidcenter.utils.ValidatorsUtil
import com.joins.kidcenter.utils.toLocalDateTimeMidnight
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import javax.persistence.EntityManager

interface LessonTemplateService : FindOneService<LessonTemplateDto>, SaveService<LessonTemplateSaveDto, SaveResult<LessonTemplate>>, DeleteOneService {
    fun findAll(searchRequest: TextSearchRequest): SearchResult<LessonTemplate>

    fun forDates(startDate: LocalDate, endDate: LocalDate): List<LessonTemplateDto>

    fun init(template: LessonTemplateInitDto): SaveResult<LessonTemplate>
}

@Service
@Transactional
open class LessonTemplateServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: LessonTemplateRepository
) : EntityService(em, queryBuilder), LessonTemplateService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): LessonTemplateDto? {
        val template = repository.findOne(id)
        if (template != null) {
            return LessonTemplateDto.fromDomainObject(template)
        }
        return null
    }

    @Transactional(readOnly = true)
    override fun forDates(startDate: LocalDate, endDate: LocalDate): List<LessonTemplateDto> =
            repository.findBetweenDates(startDate, endDate)
                    .map { LessonTemplateDto.fromDomainObject(it) }


    override fun init(template: LessonTemplateInitDto): SaveResult<LessonTemplate> {
        val result = ValidatorsUtil.validateLessonTemplate(template)
        if (result.hasErrors()) {
            return result
        }

        val overlappingTemplates = repository.findBetweenDates(template.startDate, template.endDate)

        if (!overlappingTemplates.isEmpty()) {

            val editors = overlappingTemplates.map {
                if (it.endDate > template.endDate && it.startDate <= template.endDate && it.startDate >= template.startDate) {
                    OverlappedStartTemplateEditor(it, repository, DateRange(it.startDate, template.endDate))
                } else if (it.startDate < template.startDate && it.endDate >= template.startDate && it.endDate <= template.endDate) {
                    OverlappedEndTemplateEditor(it, repository, DateRange(template.startDate, it.endDate))
                } else if (it.startDate >= template.startDate && it.endDate <= template.endDate) {
                    OverlappedFullTemplateEditor(it, repository, DateRange(it.startDate, it.endDate))
                } else {
                    OverlappedMiddleTemplateEditor(it, repository, DateRange(template.startDate, template.endDate))
                }
            }
            editors.forEach(AbstractOverlappedTemplateEditor::edit)
            val emf = em.entityManagerFactory
            val cb = emf.criteriaBuilder


            val lessonSlots = cb.createQuery()
            val root = lessonSlots.from(LessonSlot::class.java)

            val predicates = editors.map { editor ->
                cb.between(root.get("dateTime"), editor.overlappedRange.startDate.toLocalDateTimeMidnight(), editor.overlappedRange.endDate.toLocalDateTimeMidnight())
            }

            val lessonSlotIds = em.createQuery(lessonSlots.select(root.get("id")).where(cb.or(*predicates.toTypedArray()))).resultList

            if (!lessonSlotIds.isEmpty()) {
                em.createQuery("update LessonSlot ls set ls.removed=:removed where ls.id in (:lessonSlotIds)")
                        .setParameter("removed", true)
                        .setParameter("lessonSlotIds", lessonSlotIds)
                        .executeUpdate()

                @Suppress("UNCHECKED_CAST")
                val restoredLessonsByCard: List<Array<Any>> = em.createQuery("select ss.student.id, count(ss.id) from StudentSlot ss where ss.lesson.id in (:lessonSlotIds) and ss.status!=:status group by ss.student.id")
                        .setParameter("status", StudentSlotStatus.removed)
                        .setParameter("lessonSlotIds", lessonSlotIds)
                        .resultList as List<Array<Any>>
                //map<studentId, restoredLessonCount>
                val restoredLessonsMap: Map<Long, Long> = restoredLessonsByCard.map { it -> Pair(it[0] as Long, it[1] as Long) }.toMap(mutableMapOf())


                @Suppress("UNCHECKED_CAST")
                val bonusCards: List<Array<Any>> = em.createQuery("select s.id, max(card.id) from Card c join Student s on c.student.id = s.id where c.visitType = :visitType group by s.id")
                        .setParameter("visitType", VisitType.bonus)
                        .resultList as List<Array<Any>>

                bonusCards.map { it -> StudentIdCardId(it[0] as Long, it[1] as Long) }
                        .forEach { studentCard ->
                            em.createQuery("update Card c set c.lessonsLimit = c.lessonsLimit + :restoredCount where c.id = :id")
                                    .setParameter("id", studentCard.cardId)
                                    .setParameter("restoredCount", restoredLessonsMap[studentCard.studentId] ?: 0)
                                    .executeUpdate()

                        }

                em.createQuery("update StudentSlot ss set ss.status=:status where ss.lesson.id in (:lessonSlotIds)")
                        .setParameter("status", StudentSlotStatus.removed)
                        .setParameter("lessonSlotIds", lessonSlotIds)
                        .executeUpdate()
            }
        }

        val templateEntity = LessonTemplate().apply {
            name = template.name
            startDate = template.startDate
            endDate = template.endDate
        }

        return SaveResult(repository.save(templateEntity))
    }

    override fun save(entity: LessonTemplateSaveDto): SaveResult<LessonTemplate> {
        val lessonTemplate = LessonTemplate().apply {
            name = entity.name
            lessons.addAll(entity.lessons)
        }
        repository.save(lessonTemplate)
        return SaveResult(lessonTemplate)
    }

    override fun findAll(searchRequest: TextSearchRequest): SearchResult<LessonTemplate> {
        return findByRequest(searchRequest, LessonTemplate::class.java, listOf("name"))
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}