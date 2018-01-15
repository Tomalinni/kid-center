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
import com.joins.kidcenter.domain.StudentSlotStatus.*
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.dto.lessons.LessonSlotIds
import com.joins.kidcenter.repository.*
import com.joins.kidcenter.service.HomeworkService
import com.joins.kidcenter.service.StudentCardService
import com.joins.kidcenter.service.exceptions.OperationException
import com.joins.kidcenter.service.lessons.LessonEventType.*
import com.joins.kidcenter.service.mail.AppMailService
import com.joins.kidcenter.service.maps.StudentNextLessonsProvider
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.EnumUtils
import com.joins.kidcenter.utils.toSqlTimestamp
import org.apache.commons.lang3.Validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.time.LocalDateTime
import javax.persistence.EntityManager
import kotlin.reflect.KMutableProperty0

interface LessonSlotService {
    fun planLessons(request: PlanLessonsRequest): PlanLessonsResponse

    fun lessonSlotsForDates(startDate: LocalDateTime, endDate: LocalDateTime): List<LessonSlotDto>

    fun studentsForDataRequest(startDate: LocalDateTime, endDate: LocalDateTime, studentId: Long?): List<LessonStudentDto>

    fun visitLesson(request: StudentSlotRequest): LessonIdsResponse

    fun missLesson(request: StudentSlotRequest): LessonIdsResponse

    fun cancelLesson(request: RepeatedStudentSlotRequest): LessonIdsResponse

    fun revokeLesson(request: LessonSlotRequest): OperationResponse

    fun closeLesson(request: LessonSlotRequest): OperationResponse

    fun closeLessonsByDateTimeAndSubjects(lessonStartDateTime: LocalDateTime, subjects: Collection<LessonSubject>)

    fun suspendLessons(request: SuspendLessonsRequest): LessonIdsResponse

    fun unplanLesson(request: RepeatedStudentSlotRequest): LessonIdsResponse

    fun transferLessons(request: TransferLessonsRequest): LessonIdsResponse

    fun findOne(id: String): LessonSlot?

    fun setPresenceInSchool(request: SetPresenceInSchoolRequest): OperationResponse

    fun findPlannedLessonsByStudent(studentId: Long): List<StudentDashboardLesson>
}

@Service
@Transactional
open class LessonSlotServiceImpl @Autowired constructor(
        val lessonPlanner: LessonPlanner,
        val em: EntityManager,
        val studentRepository: StudentRepository,
        val studentCardRepository: StudentCardRepository,
        val lessonSlotRepository: LessonSlotRepository,
        val studentSlotRepository: StudentSlotRepository,
        val schoolVisitRepository: SchoolVisitRepository,
        val studentCardService: StudentCardService,
        val nextLessonsProvider: StudentNextLessonsProvider,
        val homeworkService: HomeworkService,
        val mailService: AppMailService,
        @Qualifier("lessonEventListeners")
        val lessonEventListener: LessonEventListener
) : LessonSlotService {

    val log: Logger = LoggerFactory.getLogger(LessonSlotService::class.java)

    override fun findOne(id: String): LessonSlot? {
        return lessonSlotRepository.findOne(id)
    }

    override fun planLessons(request: PlanLessonsRequest): PlanLessonsResponse {
        return lessonPlanner.planLessons(request)
    }


    @Transactional(readOnly = true)
    override fun lessonSlotsForDates(startDate: LocalDateTime, endDate: LocalDateTime): List<LessonSlotDto> {
        return lessonSlotRepository.findForDates(startDate, endDate).map { lessonSlot ->
            LessonSlotDto().apply {
                id = lessonSlot.id
                status = lessonSlot.status
                studentSlots = lessonSlot.students.map { studentSlot ->
                    Pair(studentSlot.id!!, StudentSlotDto.fromDomainObject(studentSlot))
                }.toMap()
                visitsSummary = VisitsSummaryDto.fromDomainObject(lessonSlot.students)
            }
        }
    }

    @Transactional(readOnly = true)
    override fun studentsForDataRequest(startDate: LocalDateTime, endDate: LocalDateTime, studentId: Long?): List<LessonStudentDto> {
        val presentStudentIds = schoolVisitRepository.findByDate(DateTimeUtils.currentDate()).map {
            Pair((it[0] as BigInteger).toLong(), (it[1] as BigInteger).toLong())
        }.toMap().values
        val studentIdsByLessons = lessonSlotRepository.findStudentIdsForDates(startDate, endDate).map(Long::toLong)
        var ids = setOf<Long>().plus(presentStudentIds).plus(studentIdsByLessons)
        if (studentId != null) {
            ids = ids.plus(studentId)
        }

        if (ids.isNotEmpty()) {
            return studentRepository.findAll(ids).map { LessonStudentDto.fromDomainObject(it, presentStudentIds.contains(it.id)) }
        }
        return emptyList()
    }

    override fun visitLesson(request: StudentSlotRequest): LessonIdsResponse {
        return changeStudentSlotStatus(request, setOf(planned, missed), visited)
    }

    override fun missLesson(request: StudentSlotRequest): LessonIdsResponse {
        return changeStudentSlotStatus(request, setOf(planned, visited), missed)
    }

    private fun changeStudentSlotStatus(studentSlotDto: StudentSlotRequest, acceptableStatuses: Set<StudentSlotStatus>, targetStatus: StudentSlotStatus): LessonIdsResponse {
        Validate.isTrue(arrayOf(visited, missed).contains(targetStatus), "One of statuses: visited, missed is expected when changing status.")
        val studentSlot = studentSlotRepository.findOne(studentSlotDto.slotId)

        @Suppress("IfNullToElvis")
        if (studentSlot == null) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.slot.not.found", studentSlotDto.slotId)
        }
        val sourceStatus = studentSlot.status

        val lesson = studentSlot.lesson!!
        if (lesson.dateTime.isAfter(DateTimeUtils.currentDateTime())) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.not.started", lesson.id)
        }

        if (lesson.status == LessonSlotStatus.closed) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.already.closed", lesson.id)
        }

        if (!acceptableStatuses.contains(sourceStatus)) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.illegal.status", studentSlot.businessId(), sourceStatus)
        }

        if (studentSlot.invalidated) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.invalidated")
        }

        if (sourceStatus != targetStatus) {
            studentSlot.setStatus(targetStatus)
            studentSlotRepository.save(studentSlot)

            val studentCard = studentCardRepository.findOne(studentSlot.card!!.id)
            val student = studentRepository.findOne(studentSlot.student!!.id)

            if (targetStatus == visited) {
                if (sourceStatus == missed && studentCard.missAvailable > 0) {
                    studentCard.missAvailable++
                    studentCard.lessonsAvailable--
                }
            } else {
                if (studentCard.missAvailable > 0) {
                    studentCard.missAvailable--
                    studentCard.lessonsAvailable++
                }
            }
            em.flush()

            if (targetStatus == visited) {
                lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, setOf(studentSlot.id!!), setOf(lesson.id), visit))
            } else {
                lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, setOf(studentSlot.id!!), setOf(lesson.id), miss))
            }

            studentCardService.updateStudentLessonTotalCounts(student.id!!)
            studentCardService.updateStudentCardLessonTotalCounts(studentCard.id!!)
            nextLessonsProvider.invalidateStudentLessons(student.id!!)
        }

        return LessonIdsResponse(findPlannedLessonsByStudent(studentSlot.student!!.id!!))
    }

    override fun cancelLesson(request: RepeatedStudentSlotRequest): LessonIdsResponse {
        val startingStudentSlot = studentSlotRepository.findOne(request.slotId)

        if (startingStudentSlot == null) {
            return LessonIdsResponse("common.error.lessons.change.lesson.status.slot.not.found", request.slotId)
        }

        val studentSlotsToCancel: Map<Long, String> = if (request.repeatWeekly) findStudentSlotsToCancel(startingStudentSlot) else mapOf(Pair(request.slotId, startingStudentSlot.lesson!!.id))
        var cancelsAvailableDecremented = false

        val studentSlots = studentSlotRepository.findAll(studentSlotsToCancel.keys).toList()
        studentSlots.forEach { studentSlot ->
            if (studentSlot.status != planned) {
                return LessonIdsResponse("common.error.lessons.cancel.illegal.status", studentSlot.businessId())
            }
            val lesson = studentSlot.lesson!!
            val card = studentSlot.card!!

            val schoolVisit = schoolVisitRepository.findByStudentAndDate(studentSlot.student!!, lesson.dateTime.toLocalDate())
            val lessonCancelType = LessonCancelType.forLesson(lesson, schoolVisit != null)
            if (lessonCancelType == null) {
                if (studentSlots.size == 1) {
                    return LessonIdsResponse("common.error.lessons.cancel.expired", lesson.id)
                } //else skip lesson, continue to cancel other lessons
            } else {
                val cancelsProp: KMutableProperty0<Int> = card.cancelsAvailableProperty(lessonCancelType)
                if (cancelsProp.get() > 0) {
                    if (!cancelsAvailableDecremented) {
                        cancelsProp.set(cancelsProp.get() - 1)
                        cancelsAvailableDecremented = true
                    }

                    card.lessonsAvailable++
                    studentSlot.setStatus(canceled, lessonCancelType)
                } else {
                    if (request.confirmed) {
                        studentSlot.setStatus(missed, lessonCancelType)
                        studentSlot.invalidated = true
                    } else {
                        if (studentSlots.size == 1) {
                            return LessonIdsResponse(OperationResponse.warning("common.lessons.cancel.confirm.invalidate"))
                        } //else skip lesson, continue to cancel other lessons
                    }
                }
            }
        }

        studentSlotRepository.save(studentSlots)
        refreshStudentCardRepeatsLeft(startingStudentSlot.card!!.id!!)

        em.flush()

        val student = studentRepository.findOne(startingStudentSlot.student!!.id)
        lessonEventListener.onLessonEvent(LessonEvent(startingStudentSlot.card!!.id!!, studentSlotsToCancel.keys, studentSlotsToCancel.values.toSet(), cancel))
        studentCardService.updateStudentLessonTotalCounts(student.id!!)
        studentCardService.updateStudentCardLessonTotalCounts(startingStudentSlot.card!!.id!!)
        nextLessonsProvider.invalidateStudentLessons(student.id!!)

        return LessonIdsResponse(findPlannedLessonsByStudent(student.id!!))
    }

    private fun findStudentSlotsToCancel(studentSlot: StudentSlot): Map<Long, String> {
        val lessonIds = LessonSlotIds.fromString(listOf(studentSlot.lesson!!.id))
        val lessonDateTime = lessonIds.minDateTime()!! //lessonIds is not empty
        val relativeLessonIds = lessonIds.lessonIds.map { it.relativeId() }.toList()
        val lessonStatuses = EnumUtils.ordinals(StudentSlotStatus.values().toMutableList().minus(StudentSlotStatus.planned).toTypedArray())
        return studentSlotRepository.findStudentSlotsWithRepeatedInLessonsIdNotInStatusFromDateTime(studentSlot.card!!.id!!, relativeLessonIds, lessonStatuses, lessonDateTime.toSqlTimestamp())
                .map { result ->
                    val lessonId = result[0] as String
                    val studentSlotId = (result[1] as BigInteger).toLong()
                    Pair(studentSlotId, lessonId)
                }.toMap()
    }

    override fun revokeLesson(request: LessonSlotRequest): OperationResponse {
        val lessonId = request.lessonId
        val createLessonResult = lessonPlanner.findOrCreateLesson(lessonId)
        if (!createLessonResult.hasPayload()) {
            return OperationResponse.fromOperationResult(createLessonResult)
        }
        val lesson = createLessonResult.payload!!

        if (lesson.status != LessonSlotStatus.planned) {
            return OperationResponse.error("common.error.lessons.common.illegal.status.planned.expected", lesson.id)
        }

        if (!Config.backDateRevokeEnabled && lesson.dateTime.isBefore(DateTimeUtils.currentDateTime())) {
            return OperationResponse.error("common.error.lessons.common.already.started", lessonId)
        }

        lesson.status = LessonSlotStatus.revoked
        lesson.students.forEach { studentSlot ->
            if (studentSlot.status != canceled) {
                studentSlot.setStatus(revoked)
                studentSlot.card!!.lessonsAvailable++

                refreshStudentCardRepeatsLeft(studentSlot.card!!.id!!)
            }
        }
        lessonSlotRepository.save(lesson)

        em.flush()

        lesson.students.forEach { studentSlot ->
            val studentCard = studentCardRepository.findOne(studentSlot.card!!.id)
            lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, setOf(studentSlot.id!!), setOf(lesson.id), revoke))
            studentCardService.updateStudentCardLessonTotalCounts(studentCard.id!!)
        }
        lesson.students.forEach {
            val student = it.student!!
            studentCardService.updateStudentLessonTotalCounts(student.id!!)
            nextLessonsProvider.invalidateStudentLessons(student.id!!)
        }

        return OperationResponse.success()
    }

    override fun closeLesson(request: LessonSlotRequest): OperationResponse {
        val lessonId = request.lessonId
        val createLessonResult = lessonPlanner.findOrCreateLesson(lessonId)
        if (!createLessonResult.hasPayload()) {
            return OperationResponse.fromOperationResult(createLessonResult)
        }
        val lesson = createLessonResult.payload!!

        return closeLesson(lesson)
    }

    override fun closeLessonsByDateTimeAndSubjects(lessonStartDateTime: LocalDateTime, subjects: Collection<LessonSubject>) {
        if (lessonStartDateTime.isAfter(DateTimeUtils.currentDateTime())) {
            throw OperationException(OperationResponse.error("common.error.close.lessons.not.started", DateTimeUtils.lessonDateTimeToString(lessonStartDateTime)), HttpStatus.BAD_REQUEST)
        }
        lessonSlotRepository.findByDateTimeAndStatusAndSubjectIn(lessonStartDateTime, LessonSlotStatus.planned, subjects).forEach { lessonSlot ->
            val result = closeLesson(lessonSlot)
            if (!result.success()) {
                log.error("Error while closing lesson ${lessonSlot.id} from scheduler: ${result.error}")
            }
        }
    }

    private fun closeLesson(lesson: LessonSlot): OperationResponse {
        if (lesson.status != LessonSlotStatus.planned) {
            return OperationResponse.error("common.error.lessons.common.illegal.status.planned.expected", lesson.id)
        }

        if (lesson.dateTime.isAfter(DateTimeUtils.currentDateTime())) {
            return OperationResponse.error("common.error.lessons.not.started", lesson.id)
        }

        lesson.status = LessonSlotStatus.closed
        val lessonDate = lesson.dateTime.toLocalDate()
        lesson.students.forEach { studentSlot ->
            //If student presence/absence is not marked explicitly, he will be counted as visited
            if (studentSlot.status == planned) {
                if (Config.schoolVisitsTrackingEnabled) {
                    val schoolVisit = schoolVisitRepository.findByStudentAndDate(studentSlot.student!!, lessonDate)
                    studentSlot.setStatus(if (schoolVisit != null) visited else missed)
                } else {
                    studentSlot.setStatus(visited)
                }
            }
        }
        lessonSlotRepository.save(lesson)

        em.flush()

        lesson.students.forEach { studentSlot ->
            if (studentSlot.status == visited || studentSlot.status == missed) {
                val studentCard = studentCardRepository.findOne(studentSlot.card!!.id)
                val student = studentRepository.findOne(studentSlot.student!!.id)
                val eventType = if (studentSlot.status == visited) LessonEventType.visit else LessonEventType.miss

                lessonEventListener.onLessonEvent(LessonEvent(studentCard.id!!, setOf(studentSlot.id!!), setOf(lesson.id), eventType))
                studentCardService.updateStudentLessonTotalCounts(student.id!!)
                studentCardService.updateStudentCardLessonTotalCounts(studentCard.id!!)
                nextLessonsProvider.invalidateStudentLessons(student.id!!)
            }
        }

        //sending emails with homework
        mailService.sendHomework(lesson, homeworkService.findByLesson(lesson))
        return OperationResponse.success()
    }

    override fun suspendLessons(request: SuspendLessonsRequest): LessonIdsResponse {
        val fromDate = request.fromDate
        if (!fromDate.isAfter(DateTimeUtils.currentDate())) {
            return LessonIdsResponse("common.error.lessons.suspend.in.past", fromDate)
        }
        val card = studentCardRepository.findOne(request.cardId)
        if (card == null) {
            return LessonIdsResponse("common.error.lessons.plan.student.card.not.found")
        }
        val studentId = card.student!!.id!!
        if (card.visitType != VisitType.regular) {
            return LessonIdsResponse("common.error.lessons.suspend.not.regular.card")
        }
        if (!fromDate.isBefore(card.definedEndDate())) {
            return LessonIdsResponse("common.error.lessons.suspend.after.expiration.date", fromDate)
        }
        if (card.suspendsAvailable <= 0) {
            return LessonIdsResponse("common.error.lessons.suspend.no.suspends.available")
        }
        val deletedStudentSlotIds: MutableSet<Long> = mutableSetOf()
        val deletedLessonSlotIds: MutableSet<String> = mutableSetOf()
        studentSlotRepository.findByCardAfterDateNotInStatus(card.id!!, fromDate, EnumUtils.ordinals(arrayOf(canceled, missed))).forEach {
            deletedStudentSlotIds.add((it[0] as BigInteger).toLong())
            deletedLessonSlotIds.add(it[1] as String)
        }
        if (deletedStudentSlotIds.isNotEmpty()) {
            studentSlotRepository.deleteByIdIn(deletedStudentSlotIds)
        }
        card.suspendsAvailable--
        card.lessonsAvailable += deletedStudentSlotIds.size
        studentCardRepository.save(card)

        refreshStudentCardRepeatsLeft(request.cardId)
        em.flush()

        lessonEventListener.onLessonEvent(LessonEvent(card.id!!, deletedStudentSlotIds, deletedLessonSlotIds, unplan))
        studentCardService.updateStudentLessonTotalCounts(studentId)
        studentCardService.updateStudentCardLessonTotalCounts(card.id!!)
        nextLessonsProvider.invalidateStudentLessons(studentId)

        return LessonIdsResponse(findPlannedLessonsByStudent(studentId))
    }

    private fun refreshStudentCardRepeatsLeft(cardId: Long) {
        clearStudentCardRepeatsLeft(cardId)
        lessonPlanner.updateStudentCardRepeatsLeft(cardId)
    }

    private fun clearStudentCardRepeatsLeft(cardId: Long) {
        studentSlotRepository.clearRepeatsLeftByCardAndStatuses(cardId, EnumUtils.ordinals(arrayOf(revoked, canceled, removed)))
    }

    override fun unplanLesson(request: RepeatedStudentSlotRequest): LessonIdsResponse {
        return lessonPlanner.unplanLesson(request)
    }

    override fun setPresenceInSchool(request: SetPresenceInSchoolRequest): OperationResponse {
        val student = studentRepository.findOne(request.studentId)
        if (student != null) {
            val setPresence = request.presentInSchool
            val currentDate = DateTimeUtils.currentDate()
            val visit = schoolVisitRepository.findByStudentAndDate(student, currentDate)

            if (visit != null && !setPresence) {
                schoolVisitRepository.delete(visit)

            } else if (visit == null && setPresence) {
                val schoolVisit = SchoolVisit().apply {
                    this.student = student
                    date = currentDate
                }
                schoolVisitRepository.save(schoolVisit)
            }
            return OperationResponse.success()
        } else {
            return OperationResponse.error("common.requested.student.not.found")
        }
    }

    override fun findPlannedLessonsByStudent(studentId: Long): List<StudentDashboardLesson> {
        return lessonPlanner.findStudentLessons(studentId)
    }

    override fun transferLessons(request: TransferLessonsRequest): LessonIdsResponse {
        return lessonPlanner.transferLessons(request)
    }
}
