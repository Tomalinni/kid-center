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
import com.joins.kidcenter.controller.PlanLessonsRequest
import com.joins.kidcenter.controller.RepeatedStudentSlotRequest
import com.joins.kidcenter.controller.SuspendLessonsRequest
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(CommonConfiguration::class, IntgTestConfiguration::class))
@ActiveProfiles(profiles = arrayOf("test"))
open class LessonsSuspendTest {
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
    fun testSuspendSingleLessons() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(2, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlotInFuture(4, 0, 0, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(2, 0, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(4, 0, 0, LessonSubject.art)
        val studentSlotId1 = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id())))
                .plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan2.id())))
                .plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        assertNotNull(studentSlotId2)

        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, DateTimeUtils.currentDate().plusDays(3)))

        LessonIdsResponseAssert(suspendResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan1, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.planned)
                .repeatsLeft(1)
                .cancelType(null)
                .invalidated(false)
        assertNull(studentSlotService!!.findByLessonAndCard(lessonIdToPlan2, studentCard.obj!!.id!!))
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 19)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 4)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(1)
                .visitedLessonsCount(0)
                .usedLessonsCount(1)
                .availableLessonsCount(19)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf(lessonIdToPlan1.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!))
    }

    @Test
    fun testSuspendCanceledLessons() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular().apply { cancelsLimit = 1 }).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(3, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlotInFuture(4, 0, 0, LessonSubject.art, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(3, 0, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(4, 0, 0, LessonSubject.art)
        val studentSlotId1 = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan1.id())))
                .plannedLessonIds[lessonIdToPlan1.id()]
        val studentSlotId2 = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan2.id())))
                .plannedLessonIds[lessonIdToPlan2.id()]
        assertNotNull(studentSlotId1)
        assertNotNull(studentSlotId2)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId1!!, true))
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId2!!, true))

        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, DateTimeUtils.currentDate().plusDays(3)))

        LessonIdsResponseAssert(suspendResponse).success()
        StudentSlotAssert(studentSlotService!!.findByLessonAndCard(lessonIdToPlan1, studentCard.obj!!.id!!)!!)
                .student(student.id!!)
                .visitType(VisitType.regular)
                .status(StudentSlotStatus.canceled)
                .repeatsLeft(1)
                .cancelType(LessonCancelType.normal)
                .invalidated(false)
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
                .cancels(1, 0)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 4)
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
    fun testSuspendLessonsInPast() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))

        val date = DateTimeUtils.currentDate().minusDays(3)
        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, date))
        LessonIdsResponseAssert(suspendResponse).error("common.error.lessons.suspend.in.past", arrayOf(date))
    }


    @Test
    fun testSuspendLessonsAfterCardExpiration() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))

        val date = DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong())
        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, date))
        LessonIdsResponseAssert(suspendResponse).error("common.error.lessons.suspend.after.expiration.date", arrayOf(date))
    }

    @Test
    fun testSuspendsNotAvailable() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular().apply { suspendsLimit = 1 }).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, DateTimeUtils.currentDate().plusDays(3)))

        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(studentCard.obj!!.id!!, DateTimeUtils.currentDate().plusDays(2)))
        LessonIdsResponseAssert(suspendResponse).error("common.error.lessons.suspend.no.suspends.available")
    }

    @Test
    fun testCardNotFound() {
        val suspendResponse = lessonSlotService!!.suspendLessons(SuspendLessonsRequest(-1, DateTimeUtils.currentDate().plusDays(2)))
        LessonIdsResponseAssert(suspendResponse).error("common.error.lessons.plan.student.card.not.found")
    }
}