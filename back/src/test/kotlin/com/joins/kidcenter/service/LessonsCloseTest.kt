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
import com.joins.kidcenter.Config
import com.joins.kidcenter.IntgTestConfiguration
import com.joins.kidcenter.controller.LessonSlotRequest
import com.joins.kidcenter.controller.PlanLessonsRequest
import com.joins.kidcenter.controller.SetPresenceInSchoolRequest
import com.joins.kidcenter.controller.StudentSlotRequest
import com.joins.kidcenter.db.DbSequenceHelper
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.*
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.service.lessons.LessonTemplateService
import com.joins.kidcenter.service.lessons.StudentSlotService
import com.joins.kidcenter.service.maps.StudentNextLessonsStorage
import com.joins.kidcenter.service.sms.scheduling.SmsSchedulingService
import com.joins.kidcenter.utils.DateTimeUtils
import org.junit.After
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
open class LessonsCloseTest {
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
        Config.schoolVisitsTrackingEnabled = true
        Config.backDateRevokeEnabled = true
    }

    @Test
    fun testCloseLessonById() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 70, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 70, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val closeResponse = lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))

        OperationResponseAssert(closeResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.missed)
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
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCloseVisitedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 70, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 70, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.visitLesson(StudentSlotRequest(studentSlotId!!))
        val closeResponse = lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))

        OperationResponseAssert(closeResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.visited)
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
                .plannedLessonsCount(0)
                .visitedLessonsCount(1)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCloseSchoolVisitedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 0, 1, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 0, 1, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.setPresenceInSchool(SetPresenceInSchoolRequest(student.id!!, true))
        val closeResponse = lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))

        OperationResponseAssert(closeResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.visited)
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
                .plannedLessonsCount(0)
                .visitedLessonsCount(1)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCloseClosedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInPast(0, 70, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(0, 70, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))
        val closeResponse = lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))

        OperationResponseAssert(closeResponse).error("common.error.lessons.common.illegal.status.planned.expected", arrayOf(lessonIdToPlan.id()))
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.missed)
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
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCloseNotStartedLesson() {
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

        val closeResponse = lessonSlotService!!.closeLesson(LessonSlotRequest(lessonIdToPlan.id()))

        OperationResponseAssert(closeResponse).error("common.error.lessons.not.started", arrayOf(lessonIdToPlan.id()))
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
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testCloseLessonByDateTime() {
        val lessonDateTime = DateTimeUtils.currentDateTime().minusMinutes(70).withSecond(0).withNano(0)
        val student1 = studentService!!.save(factories!!.student.student()).obj!!
        val student2 = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard1 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student1, card))
        val studentCard2 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student2, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(lessonDateTime, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(lessonDateTime, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotByDateTime(lessonDateTime, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotByDateTime(lessonDateTime, LessonSubject.art)
        val studentSlotId1 = lessonSlotService!!.planLessons(PlanLessonsRequest(student1.id!!, studentCard1.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id())))
                .plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = lessonSlotService!!.planLessons(PlanLessonsRequest(student2.id!!, studentCard2.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan2.id())))
                .plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        assertNotNull(studentSlotId2)
        lessonSlotService!!.visitLesson(StudentSlotRequest(studentSlotId2!!))

        lessonSlotService!!.closeLessonsByDateTimeAndSubjects(lessonDateTime, LessonSubject.values().toList())

        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan1, studentCard1.obj!!.id!!)!!)
                .student(student1.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.missed)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard1.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard1.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan2, studentCard2.obj!!.id!!)!!)
                .student(student2.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.visited)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard2.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(1)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard2.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @Test
    fun testCloseLessonByDateTimeRevokedLesson() {
        val lessonDateTime = DateTimeUtils.currentDateTime().minusMinutes(70).withSecond(0).withNano(0)
        val student1 = studentService!!.save(factories!!.student.student()).obj!!
        val student2 = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard1 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student1, card))
        val studentCard2 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student2, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(lessonDateTime, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(lessonDateTime, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotByDateTime(lessonDateTime, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotByDateTime(lessonDateTime, LessonSubject.art)
        val studentSlotId1 = lessonSlotService!!.planLessons(PlanLessonsRequest(student1.id!!, studentCard1.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id())))
                .plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = lessonSlotService!!.planLessons(PlanLessonsRequest(student2.id!!, studentCard2.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan2.id())))
                .plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        assertNotNull(studentSlotId2)
        lessonSlotService!!.revokeLesson(LessonSlotRequest(lessonIdToPlan1.id()))
        lessonSlotService!!.visitLesson(StudentSlotRequest(studentSlotId2!!))

        lessonSlotService!!.closeLessonsByDateTimeAndSubjects(lessonDateTime, LessonSubject.values().toList())

        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan1, studentCard1.obj!!.id!!)!!)
                .student(student1.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.revoked)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard1.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard1.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan2, studentCard2.obj!!.id!!)!!)
                .student(student2.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.visited)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        StudentCardAssert(studentCardService!!.findOne(studentCard2.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(1)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard2.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf())
    }

    @After
    fun tearDown() {
        Config.schoolVisitsTrackingEnabled = false
        Config.backDateRevokeEnabled = false
    }
}
