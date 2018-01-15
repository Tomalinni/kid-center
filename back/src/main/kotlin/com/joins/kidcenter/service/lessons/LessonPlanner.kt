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

import com.joins.kidcenter.Config
import com.joins.kidcenter.controller.*
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.StudentDashboardLesson
import com.joins.kidcenter.dto.StudentLessonsTimeCategory
import com.joins.kidcenter.dto.internal.OperationResult
import com.joins.kidcenter.dto.internal.ParametrizedMessage
import com.joins.kidcenter.dto.lessons.*
import com.joins.kidcenter.repository.*
import com.joins.kidcenter.service.StudentCardService
import com.joins.kidcenter.service.StudentService
import com.joins.kidcenter.service.exceptions.OperationException
import com.joins.kidcenter.service.maps.StudentNextLessonsProvider
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.EnumUtils
import com.joins.kidcenter.utils.lessons.LessonSlotIdExtrapolator
import com.joins.kidcenter.utils.limits
import com.joins.kidcenter.utils.toSqlTimestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.util.*
import javax.persistence.EntityManager

interface LessonPlanner {
    fun planLessons(planLessonsDto: PlanLessonsRequest): PlanLessonsResponse
    fun unplanLesson(studentSlotDto: StudentSlotRequest): LessonIdsResponse
    fun transferLessons(request: TransferLessonsRequest): LessonIdsResponse
    fun findStudentLessons(studentId: Long): List<StudentDashboardLesson>
    fun updateStudentCardRepeatsLeft(cardId: Long)
    fun findOrCreateLesson(lessonId: String): OperationResult<LessonSlot>
}

@Service
@Transactional(rollbackFor = arrayOf(OperationException::class))
open class LessonPlannerImpl @Autowired constructor(
        val em: EntityManager,
        val studentRepository: StudentRepository,
        val studentCardRepository: StudentCardRepository,
        val cardRepository: CardRepository,
        val lessonTemplateRepository: LessonTemplateRepository,
        val lessonSlotRepository: LessonSlotRepository,
        val studentSlotRepository: StudentSlotRepository,
        val studentService: StudentService,
        val studentCardService: StudentCardService,
        val nextLessonsProvider: StudentNextLessonsProvider,
        @Qualifier("lessonEventListeners")
        val lessonEventListener: LessonEventListener
) : LessonPlanner {

    private val log: Logger = LoggerFactory.getLogger(LessonPlannerImpl::class.java)
    private val decrementAvailableLessonsStatuses = listOf(StudentSlotStatus.planned, StudentSlotStatus.visited, StudentSlotStatus.missed)

    override fun planLessons(planLessonsDto: PlanLessonsRequest): PlanLessonsResponse {
        val lessonIdsToPlan = findLessonsToPlan(planLessonsDto)
        if (lessonIdsToPlan.isEmpty()) {
            failPlanLesson("common.error.lessons.plan.no.lessons.selected")
        }

        val student = studentRepository.findOne(planLessonsDto.studentId)
        if (student == null) {
            failPlanLesson("common.error.lessons.plan.student.not.found")
        }

        val studentCard = studentCardRepository.findOne(planLessonsDto.cardId)
        if (studentCard == null) {
            failPlanLesson("common.error.lessons.plan.student.card.not.found")
        }

        val skippedLessons: MutableMap<String, ParametrizedMessage> = mutableMapOf()

        val lessonIdObjs = lessonIdsToPlan
                .map { id -> LessonSlotId.fromString(id) }
        val lessonDates = lessonIdObjs
                .map { id -> id.dateTime }
        val (startDate, endDate) = lessonDates.limits()!!
        val cardEndDate = studentCard.definedEndDate()
        val lessonsToPlanCount = lessonIdsToPlan.size
        val lessonsAvailable = studentCard.lessonsAvailable

        if (lessonsAvailable < lessonsToPlanCount) {
            failPlanLesson(ParametrizedMessage("common.error.lessons.plan.lessons.not.available", lessonsToPlanCount, lessonsAvailable))
        }

        val conflicts = LessonSlotIds.fromString(lessonIdsToPlan).conflicts()
        if (conflicts.isNotEmpty()) {
            val conflictGroupStrings = conflicts.map { ids -> ids.lessonIds.joinToString(prefix = "(", postfix = ")") }
            failPlanLesson(ParametrizedMessage("common.error.lessons.plan.time.subject.conflict", conflictGroupStrings.joinToString()))
        }

        if (!Config.backDatePlanningEnabled && startDate.toLocalDate().isBefore(DateTimeUtils.currentDate())) {
            failPlanLesson(ParametrizedMessage("common.error.lessons.plan.in.past"))
        }

        if (cardEndDate.isBefore(endDate.toLocalDate())) {
            failPlanLesson(ParametrizedMessage("common.error.lessons.plan.after.card.expiration.date", DateTimeUtils.dateToString(cardEndDate)))
        }

        if (student.gender == Gender.boy && lessonIdObjs.find { it.subject == LessonSubject.ballet } != null) {
            failPlanLesson("common.error.lessons.plan.assign.boy.for.ballet")
        }

        val initialPlanLessonIds: Set<String> = lessonIdsToPlan.toSet()
        val unplannableLessons = findUnplannableLessons(initialPlanLessonIds, planLessonsDto.studentId, true)
        val collectedLessonIds: MutableSet<String> = initialPlanLessonIds.minus(unplannableLessons.keys).toMutableSet()
        skippedLessons.putAll(unplannableLessons)

        if (planLessonsDto.repeatWeekly) {
            val templates = lessonTemplateRepository.findBetweenDates(startDate.toLocalDate(), endDate.toLocalDate())
            if (templates.size > 1) {
                failPlanLesson("common.error.lessons.plan.plan.from.several.templates", skippedLessons)
            } else if (templates.isEmpty()) {
                failPlanLesson("common.error.lessons.plan.no.templates.found", skippedLessons)
            }

            val extrapolator = LessonSlotIdExtrapolator(lessonIdObjs, DateTimeUtils.min(cardEndDate, templates[0].endDate))
            var lessonsRemaining = lessonsAvailable - collectedLessonIds.size

            while (lessonsRemaining > 0) {
                val nextLessonIds = extrapolator.next(lessonsRemaining).map { it.id() }
                val lessonIdsEnded = nextLessonIds.size < lessonsRemaining

                if (!nextLessonIds.isEmpty()) {
                    val nextSkippedLessons = findUnplannableLessons(nextLessonIds, planLessonsDto.studentId, true)
                    val filteredLessonIds = nextLessonIds.minus(nextSkippedLessons.keys)
                    collectedLessonIds.addAll(filteredLessonIds)
                    skippedLessons.putAll(nextSkippedLessons)
                    lessonsRemaining -= filteredLessonIds.size
                }

                if (lessonIdsEnded) {
                    break
                }
            }
        }

        if (collectedLessonIds.isEmpty()) {
            failPlanLesson("common.error.lessons.plan.no.lessons.can.be.planned", skippedLessons)
        }

        studentSlotRepository.deleteSlotsByStudentInLessonsInStatus(planLessonsDto.studentId, collectedLessonIds, StudentSlotStatus.canceled.ordinal)

        val plannedLessons: MutableMap<String, Long> = createStudentSlots(collectedLessonIds, studentCard, planLessonsDto.repeatWeekly)

        studentCard.lessonsAvailable -= collectedLessonIds.size
        studentCardRepository.save(studentCard)

        em.flush()

        lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, plannedLessons.values.toSet(), plannedLessons.keys.toSet(), LessonEventType.plan))
        studentCardService.updateStudentLessonTotalCounts(student.id!!)
        studentCardService.updateStudentCardLessonTotalCounts(studentCard.id!!)
        nextLessonsProvider.invalidateStudentLessons(student.id!!)

        return PlanLessonsResponse(true, plannedLessons, skippedLessons, findStudentLessons(planLessonsDto.studentId))

    }

    override fun unplanLesson(studentSlotDto: StudentSlotRequest): LessonIdsResponse {
        val studentSlot = studentSlotRepository.findOne(studentSlotDto.slotId)

        if (studentSlot == null) {
            return LessonIdsResponse("common.error.lessons.unplan.slot.not.found", studentSlotDto.slotId.toString())
        }
        val studentCard = studentSlot.card!!
        val studentId = studentSlot.student!!.id!!

        val response: UnplanLessonResponse
        if (studentSlot.isRepeated()) {
            response = unplanMultipleLessons(studentCard, LessonSlotIds.fromString(listOf(studentSlot.lesson!!.id)))
        } else {
            response = unplanSingleLesson(studentCard, studentSlot)
        }

        studentCardRepository.save(studentCard)
        updateStudentCardRepeatsLeft(studentCard.id!!)
        em.flush()

        val unplannedStudentSlotIds = response.unplannedStudentSlots.map { it.slotId }.toSet()
        val unplannedLessonIds = response.unplannedStudentSlots.map { it.lessonId }.toSet()
        lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, unplannedStudentSlotIds, unplannedLessonIds, LessonEventType.unplan))

        studentCardService.updateStudentLessonTotalCounts(studentId)
        studentCardService.updateStudentCardLessonTotalCounts(studentCard.id!!)
        nextLessonsProvider.invalidateStudentLessons(studentId)

        return LessonIdsResponse(OperationResponse.success(), findStudentLessons(studentId))
    }

    override fun findStudentLessons(studentId: Long): List<StudentDashboardLesson> {
        val visitTypes = VisitType.values()
        val statuses = arrayOf(StudentSlotStatus.planned, StudentSlotStatus.canceled, StudentSlotStatus.visited, StudentSlotStatus.missed)
        return studentService.findStudentLessons(studentId, visitTypes, statuses, DateTimeUtils.currentDate(), StudentLessonsTimeCategory.schedule)
    }

    override fun updateStudentCardRepeatsLeft(cardId: Long) {
        val lessonIdsToSlotIds = studentSlotRepository.findStudentCardLessonIdsAndSlotIdsBySlotStatusIn(cardId, listOf(StudentSlotStatus.planned.ordinal))
                .map {
                    val lessonId = it[0] as String
                    val slotId = (it[1] as BigInteger).toLong()
                    Pair(lessonId, slotId)
                }.toMap()
        val repeatsLeftMap = createLessonsRepeatsLeftMap(lessonIdsToSlotIds.keys)

        lessonIdsToSlotIds.entries.forEach {
            val lessonId = it.key
            val slotId = it.value
            studentSlotRepository.updateRepeatsLeftInSlot(slotId, repeatsLeftMap[LessonSlotId.fromString(lessonId)] ?: 1)
        }
    }

    private fun createStudentSlots(collectedLessonIds: MutableSet<String>, studentCard: StudentCard, repeatWeekly: Boolean): MutableMap<String, Long> {
        val lessonRepeatsLeft: SortedMap<LessonSlotId, Int> = if (repeatWeekly) createLessonsRepeatsLeftMap(collectedLessonIds) else sortedMapOf()
        val lessonSlotsMap = lessonSlotRepository.findAll(collectedLessonIds).map { Pair(it.id, it) }.toMap()
        val plannedLessons: MutableMap<String, Long> = mutableMapOf()
        collectedLessonIds.forEach { lessonSlotId ->
            var lessonSlot = lessonSlotsMap[lessonSlotId]
            if (lessonSlot == null) {
                val lessonSlotResult = findOrCreateLesson(lessonSlotId)
                if (!lessonSlotResult.hasPayload()) {
                    failPlanLesson(ParametrizedMessage("common.error.lessons.plan.lesson.is.not.in.schedule", lessonSlotId), emptyMap())
                }
                lessonSlot = lessonSlotResult.payload
            }
            val studentSlot = StudentSlot().apply {
                this.student = studentCard.student!!
                this.lesson = lessonSlot
                this.card = studentCard
                this.visitType = studentCard.visitType
                setStatus(StudentSlotStatus.planned)
                repeatsLeft = lessonRepeatsLeft[LessonSlotId.fromString(lessonSlotId)] ?: 1
            }
            lessonSlot!!.students.add(studentSlot)
            //studentSlot will change id after save
            plannedLessons[lessonSlotId] = studentSlotRepository.save(studentSlot)!!.id!!
        }
        return plannedLessons
    }

    private fun mapSlotStatus(result: Array<Any>): Pair<String, SlotStatus> {
        val lessonId = result[0] as String
        val studentSlotId = (result[1] as BigInteger).toLong()
        val status = StudentSlotStatus.values()[result[2] as Int]
        val cancelType = EnumUtils.nullableByOrdinal(result[3] as Int?, LessonCancelType.values(), null)
        return Pair(lessonId, SlotStatus(lessonId, studentSlotId, status, cancelType))
    }

    private fun findLessonsToPlan(planLessonsDto: PlanLessonsRequest): List<String> {
        val lessonIds = planLessonsDto.lessonIds.toList()
        if (lessonIds.isNotEmpty()) {
            val alreadyPlannedStudentLessons = studentSlotRepository.findStudentLessonIdsInLessonIdsInStatus(planLessonsDto.studentId, lessonIds, StudentSlotStatus.planned.ordinal)
            return lessonIds.minus(alreadyPlannedStudentLessons)
        }
        return lessonIds
    }

    private fun failPlanLesson(error: String, skippedLessons: Map<String, ParametrizedMessage> = emptyMap()) {
        failPlanLesson(ParametrizedMessage(error), skippedLessons)
    }

    private fun failPlanLesson(errorMessage: ParametrizedMessage, skippedLessons: Map<String, ParametrizedMessage> = emptyMap()) {
        val response = PlanLessonsResponse(skippedLessons = skippedLessons, response = OperationResponse.error(errorMessage))
        throw OperationException(response, HttpStatus.OK)
    }

    private fun failTransferLesson(message: String, vararg params: Any) {
        throw OperationException(LessonIdsResponse(message, *params), HttpStatus.OK)
    }

    private fun createLessonsRepeatsLeftMap(collectedLessonIds: Set<String>): SortedMap<LessonSlotId, Int> {
        val lessonIdToRepeatsLeft: SortedMap<LessonSlotId, Int> = sortedMapOf()
        collectedLessonIds.map { LessonSlotId.fromString(it) }.toSortedSet().reversed().forEach { id ->
            lessonIdToRepeatsLeft.put(id, findLessonRepeatsLeft(id, lessonIdToRepeatsLeft))
        }
        return lessonIdToRepeatsLeft
    }

    private fun findLessonRepeatsLeft(lessonId: LessonSlotId, nextLessonsRepeatsLeft: SortedMap<LessonSlotId, Int>): Int {
        val iter = nextLessonsRepeatsLeft.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (lessonId.hasSameDayTimeAndSubject(entry.key)) {
                return entry.value + 1
            }
        }
        return 1
    }

    private fun findUnplannableLessons(lessonIds: Collection<String>, studentId: Long, restrictSameLessonTime: Boolean): Map<String, ParametrizedMessage> {
        val summaryById: Map<String, PlannedLessonSlotSummary> = lessonSlotRepository.findSummaryForIds(lessonIds, studentId)
                .map {
                    val lessonSlotId = it[0] as String
                    val status = it[1] as Int?
                    val revoked = LessonSlotStatus.revoked.ordinal == status
                    val regularStudentsCount = (it[2] as BigInteger).toInt()
                    val anyStudentCount = (it[3] as BigInteger).toInt()
                    val slotAlreadyUsed = (it[4] as BigInteger).toInt() > 0

                    Pair(lessonSlotId, PlannedLessonSlotSummary(lessonSlotId, revoked, regularStudentsCount, anyStudentCount, slotAlreadyUsed))
                }
                .toMap()

        val lessonTimesCount: Map<String, Int> = if (restrictSameLessonTime) {
            lessonSlotRepository.findLessonsTimesCountByIdsNotInStatus(lessonIds.map { it.substring(1) }, studentId, EnumUtils.ordinals(arrayOf(StudentSlotStatus.canceled, StudentSlotStatus.revoked, StudentSlotStatus.removed, StudentSlotStatus.missed)))
                    .map { Pair(it[0] as String, (it[1] as BigInteger).toInt()) }
                    .toMap()
        } else mapOf()

        val unplannableLessons = mutableMapOf<String, ParametrizedMessage>()
        lessonIds.forEach { id ->
            val summary = summaryById[id]
            // not persisted lesson slots are not revoked
            if (summary != null) {
                if (summary.revoked) {
                    unplannableLessons[id] = ParametrizedMessage("common.error.lessons.plan.lesson.revoked")
                } else if (summary.regularStudentsCount >= 8) {
                    unplannableLessons[id] = ParametrizedMessage("common.error.lessons.plan.lesson.regular.slots.limit.exceeded")
                } else if (summary.anyStudentCount >= 10) {
                    unplannableLessons[id] = ParametrizedMessage("common.error.lessons.plan.lesson.slots.limit.exceeded")
                } else if (summary.studentAlreadyPlanned) {
                    unplannableLessons[id] = ParametrizedMessage("common.error.lessons.plan.lesson.already.used")
                }
            }
            val lessonTimeCount = lessonTimesCount[id.substring(1)]
            if (lessonTimeCount != null && lessonTimeCount > 0) {
                unplannableLessons[id] = ParametrizedMessage("common.error.lessons.plan.lesson.time.already.used")
            }
        }
        return unplannableLessons.toMap()
    }

    override fun findOrCreateLesson(lessonId: String): OperationResult<LessonSlot> {
        val lesson = lessonSlotRepository.findOne(lessonId)

        if (lesson == null) {
            val lessonSlotId = LessonSlotId.fromString(lessonId)
            val templates = lessonTemplateRepository.findBetweenDates(lessonSlotId.dateTime.toLocalDate(), lessonSlotId.dateTime.toLocalDate())
            if (templates.isEmpty()) {
                return OperationResult.error("common.error.lessonId.has.not.template", lessonId)
            }
            assert(templates.size == 1)
            val template = templates[0]

            val templateLessonSlot = template.lessons.find { templateLesson -> lessonSlotId.matchesTemplate(templateLesson) }
            if (templateLessonSlot != null) {
                val newLesson = LessonSlot().apply {
                    id = lessonId
                    ageGroup = templateLessonSlot.ageGroup
                    subject = lessonSlotId.subject
                    dateTime = lessonSlotId.dateTime
                }
                return OperationResult(lessonSlotRepository.save(newLesson))
            }
            return OperationResult.error("common.error.lessonId.is.not.in.template", lessonId)
        }

        return OperationResult(lesson)
    }

    private fun unplanMultipleLessons(studentCard: StudentCard, lessonIds: LessonSlotIds): UnplanLessonResponse {
        val statuses = doUnplanMultipleLessons(studentCard, lessonIds, true)

        return UnplanLessonResponse().apply {
            unplannedStudentSlots = statuses.toList()
            studentPlannedLessons = findStudentLessons(studentCard.student!!.id!!)
        }
    }

    private fun doUnplanMultipleLessons(studentCard: StudentCard, lessonIds: LessonSlotIds, revertAvailableLessonsCount: Boolean): SlotStatuses {
        val statuses = SlotStatuses()
        val lessonDateTime = lessonIds.minDateTime()!! //lessonIds is not empty
        val relativeLessonIds = lessonIds.lessonIds.map { it.relativeId() }.toList()
        val repeatedSlotIdsToUnplan = studentSlotRepository.findStudentSlotsWithRepeatedInLessonsIdFromDateTime(studentCard.id!!, relativeLessonIds, lessonDateTime.toSqlTimestamp())
                .map { mapSlotStatus(it) }.toMap()
        statuses.add(repeatedSlotIdsToUnplan)

        if (!statuses.isEmpty()) {
            if (revertAvailableLessonsCount) {
                studentCard.lessonsAvailable += statuses.lessonsCountByStatuses(decrementAvailableLessonsStatuses)
                if (studentCard.missAvailable > 0) {
                    studentCard.missAvailable += statuses.lessonsCountByStatuses(listOf(StudentSlotStatus.missed))
                }

                LessonCancelType.values().forEach {
                    val property = studentCard.cancelsAvailableProperty(it)
                    property.set(property.get() + statuses.lessonsCountByCancelType(it))
                }
            }

            studentSlotRepository.deleteByIdIn(statuses.studentSlotIds())
        }
        return statuses
    }

    private fun unplanSingleLesson(studentCard: StudentCard, studentSlot: StudentSlot): UnplanLessonResponse {
        val unplannedSlot = SlotStatus(studentSlot.lesson!!.id, studentSlot.id!!, studentSlot.status, null)
        if (decrementAvailableLessonsStatuses.contains(studentSlot.status)) { //covers also missed invalidated lesson
            studentCard.lessonsAvailable++
        }
        if (studentSlot.status == StudentSlotStatus.canceled && studentSlot.cancelType != null) {
            val cancelsProperty = studentCard.cancelsAvailableProperty(studentSlot.cancelType!!)
            cancelsProperty.set(cancelsProperty.get() + 1)
        }
        if (studentSlot.status == StudentSlotStatus.missed && studentCard.missAvailable > 0) {
            studentCard.missAvailable++
        }

        studentSlotRepository.delete(studentSlot)


        return UnplanLessonResponse().apply {
            unplannedStudentSlots = listOf(unplannedSlot)
        }
    }

    override fun transferLessons(request: TransferLessonsRequest): LessonIdsResponse {
        if (request.lessonIds.isEmpty()) {
            return LessonIdsResponse("common.error.lessons.transfer.no.lessons.selected")
        }
        if (request.studentId == request.targetStudentId) {
            return LessonIdsResponse("common.error.lessons.transfer.equal.source.and.target")
        }

        val student = studentRepository.findOne(request.studentId)
        if (student == null) {
            return LessonIdsResponse("common.error.lessons.plan.student.not.found")
        }

        val targetStudent = studentRepository.findOne(request.targetStudentId)
        if (targetStudent == null) {
            return LessonIdsResponse("common.error.lessons.transfer.target.student.not.found")
        }

        val card = studentCardRepository.findOne(request.cardId)
        if (card == null) {
            return LessonIdsResponse("common.error.lessons.plan.student.card.not.found")
        }
        if (!card.visitType.canBeTransferred) {
            return LessonIdsResponse("common.error.lessons.transfer.untransferable.card")
        }
        val plannedLessonIdsByCard = studentSlotRepository.findStudentCardLessonIdsAndSlotIdsBySlotStatusIn(card.id!!, listOf(StudentSlotStatus.planned.ordinal))
                .map { it[0] }
        if (!plannedLessonIdsByCard.containsAll(request.lessonIds)) {
            return LessonIdsResponse("common.error.lessons.transfer.lessons.for.different.cards.selected")
        }

        val transferCard = cardRepository.findOne(request.transferCardId)
        if (transferCard == null) {
            return LessonIdsResponse("common.error.lessons.transfer.transfer.card.not.found")
        }
        if (transferCard.visitType != VisitType.transfer) {
            return LessonIdsResponse("common.error.lessons.transfer.transfer.card.illegal.type")
        }

        val unplannedLessons = doUnplanMultipleLessons(card, LessonSlotIds.fromString(request.lessonIds), false)
        val previouslyPlannedLessonsCount = unplannedLessons.lessonsCountByStatus(StudentSlotStatus.planned)
        if (previouslyPlannedLessonsCount == 0) {
            failTransferLesson("common.error.lessons.transfer.no.planned.lessons.selected")
        }
        if (previouslyPlannedLessonsCount != unplannedLessons.size()) {
            failTransferLesson("common.error.lessons.transfer.not.only.planned.lessons.selected")
        }

        val targetCard = findTargetTransferCard(card).apply {
            this.student = targetStudent
            this.visitType = VisitType.transfer
            this.price = transferCard.price
            this.durationDays = transferCard.durationDays
            this.lessonsLimit += previouslyPlannedLessonsCount
            this.lessonsAvailable += previouslyPlannedLessonsCount
            this.cancelsLimit = transferCard.cancelsLimit
            this.cancelsAvailable = transferCard.cancelsLimit
            this.lateCancelsLimit = transferCard.lateCancelsLimit
            this.lateCancelsAvailable = transferCard.lateCancelsLimit
            this.lastMomentCancelsLimit = transferCard.lastMomentCancelsLimit
            this.lastMomentCancelsAvailable = transferCard.lastMomentCancelsLimit
            this.undueCancelsLimit = transferCard.undueCancelsLimit
            this.undueCancelsAvailable = transferCard.undueCancelsLimit
            this.missLimit = transferCard.missLimit
            this.missAvailable = transferCard.missLimit
            this.suspendsLimit = transferCard.suspendsLimit
            this.suspendsAvailable = transferCard.suspendsLimit
        }
        studentCardRepository.save(targetCard)

        val studentPlannedLessons = findStudentLessons(student.id!!)
        return LessonIdsResponse(studentPlannedLessons)
    }

    private fun findTargetTransferCard(sourceTransferCard: StudentCard): StudentCard {
        val cards = studentCardRepository.findBySourceTransferCardAndActivationDateNullOrderByIdAsc(sourceTransferCard)
        val targetTransferCard = if (cards.isEmpty()) StudentCard() else cards[0]
        targetTransferCard.sourceTransferCard = sourceTransferCard
        return targetTransferCard
    }
}
