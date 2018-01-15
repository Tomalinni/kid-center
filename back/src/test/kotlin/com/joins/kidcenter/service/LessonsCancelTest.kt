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

import com.joins.kidcenter.CommonConfiguration
import com.joins.kidcenter.IntgTestConfiguration
import com.joins.kidcenter.controller.LessonProcedure
import com.joins.kidcenter.controller.PlanLessonsRequest
import com.joins.kidcenter.controller.RepeatedStudentSlotRequest
import com.joins.kidcenter.controller.SetPresenceInSchoolRequest
import com.joins.kidcenter.db.DbSequenceHelper
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.lessons.StudentSlotId
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.*
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.service.lessons.LessonTemplateService
import com.joins.kidcenter.service.lessons.StudentSlotService
import com.joins.kidcenter.service.maps.StudentNextLessonsStorage
import com.joins.kidcenter.service.sms.scheduling.SmsSchedulingService
import com.joins.kidcenter.utils.DateTimeUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertNotNull


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(CommonConfiguration::class, IntgTestConfiguration::class))
@ActiveProfiles(profiles = arrayOf("test"))
open class LessonsCancelTest {
    @Autowired
    var studentService: StudentService? = null
    @Autowired
    var cardService: CardService? = null
    @Autowired
    var studentCardService: StudentCardService? = null
    @Autowired
    var lessonSlotService: LessonSlotService? = null
    @Autowired
    var studentSlotService: StudentSlotService? = null
    @Autowired
    var lessonTemplateService: LessonTemplateService? = null
    @Autowired
    var nextLessonsStorage: StudentNextLessonsStorage? = null
    @Autowired
    var smsSchedulingService: SmsSchedulingService? = null
    @Autowired
    var factories: ThFactories? = null
    @Autowired
    var dbHelper: DbSequenceHelper? = null

    @Before
    fun setUp() {
        dbHelper!!.createRequiredSequences()
        dbHelper!!.clearTables()
        dbHelper!!.resetCreatedSequences()
        nextLessonsStorage!!.invalidateAllLessons()
        smsSchedulingService!!.cancelScheduledNotifications()
    }

    @Test
    fun testNormalCancelSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(2, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(2, 0, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(1)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 4)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testLateCancelSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(0, 2, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(0, 2, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(1)
                .cancelType(LessonCancelType.late)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .lateCancels(1, 0)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testLastMomentCancelSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(DateTimeUtils.currentDayOfWeek(), 23, 59, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFutureOrToday(DateTimeUtils.currentDayOfWeek(), 23, 59, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.setPresenceInSchool(SetPresenceInSchoolRequest(student.id!!, true))

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(1)
                .cancelType(LessonCancelType.lastMoment)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 0)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testUndueCancelSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 0, 10, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 0, 10, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.setPresenceInSchool(SetPresenceInSchoolRequest(student.id!!, true))

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(1)
                .cancelType(LessonCancelType.undue)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 0)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCancelSingleLessonWithInvalidationNoConfirmation() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(0, 2, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlotInFuture(0, 3, 0, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(0, 2, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(0, 3, 0, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId1 = planResponse.plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = planResponse.plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId1!!))
        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId2!!))

        LessonIdsResponseAssert(cancelResponse).warning("common.lessons.cancel.confirm.invalidate")
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 0)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(1)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf(lessonIdToPlan2.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testCancelSingleLessonWithInvalidationWithConfirmation() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(0, 2, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlotInFuture(0, 3, 0, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(0, 2, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(0, 3, 0, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId1 = planResponse.plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = planResponse.plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId1!!))
        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId2!!, true))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan2, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.missed)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(true)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 0)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testCancelRepeatedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(2, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlotInFuture(3, 0, 0, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(2, 0, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(3, 0, 0, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, true, LessonProcedure.plan, arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan1.id()]
        assertNotNull(studentSlotId)

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!, false, true))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan1, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(10)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 10)
                .cancels(5, 4)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(10)
                .visitedLessonsCount(0)
                .usedLessonsCount(10)
                .availableLessonsCount(10)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(lessonIdToPlan2.andFollowingWeeks(3))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testCancelFinishedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 0, 70, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 0, 70, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(cancelResponse).error("common.error.lessons.cancel.expired", arrayOf(lessonIdToPlan.id()))
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.planned)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(1)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf(lessonIdToPlan.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf()) //notification is not created for lesson planned in past
    }

    @Test
    fun testCancelFinishedRepeatedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 0, 70, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 0, 70, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, true, LessonProcedure.plan, arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!, false, true))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.planned)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan.plusWeeks(1), studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(19)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 4)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(1)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf(lessonIdToPlan.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf()) //notification is not created for lesson planned in past
    }

    @Test
    fun testCancelLessonIllegalStatus() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(2, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(2, 0, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        val modifiedStudent = studentService!!.findOne(student.id!!)!!
        assertNotNull(studentSlotId)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId))

        LessonIdsResponseAssert(cancelResponse).error("common.error.lessons.cancel.illegal.status", arrayOf(StudentSlotId(lessonIdToPlan, modifiedStudent).id))
    }

    @Test
    fun testCancelRepeatedLessonIllegalStatus() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(2, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(2, 0, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, true, LessonProcedure.plan, arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        val studentSlotId2 = planResponse.plannedLessonIds[lessonIdToPlan.plusWeeks(1).id()]
        assertNotNull(studentSlotId)
        assertNotNull(studentSlotId2)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId2!!, false, false)) //cancel middle lesson in repeated sequence

        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!, false, true))

        LessonIdsResponseAssert(cancelResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(19)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan.plusWeeks(2), studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(18)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 3)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testCancelLessonInvalidSlotId() {
        val cancelResponse = lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(-1))

        LessonIdsResponseAssert(cancelResponse).error("common.error.lessons.change.lesson.status.slot.not.found", arrayOf(-1L))
    }
}