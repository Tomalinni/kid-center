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
import com.joins.kidcenter.controller.StudentSlotRequest
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
open class LessonsMissTest {
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
    fun testMissSingleLessonInPast() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

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

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        val missResponse = lessonSlotService!!.missLesson(StudentSlotRequest(studentSlotId!!))

        LessonIdsResponseAssert(missResponse).success()
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
                .isEmpty()
    }

    @Test
    fun testMissVisitedLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        lessonSlotService!!.visitLesson(StudentSlotRequest(studentSlotId!!))
        val missResponse = lessonSlotService!!.missLesson(StudentSlotRequest(studentSlotId))

        LessonIdsResponseAssert(missResponse).success()
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
                .isEmpty()
    }

    @Test
    fun testMissRepeatedInvocation() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())

        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))
        val planResponse = lessonSlotService!!.planLessons(planRequest)
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)

        lessonSlotService!!.missLesson(StudentSlotRequest(studentSlotId!!))
        val missResponse = lessonSlotService!!.missLesson(StudentSlotRequest(studentSlotId))
        val modifiedStudent: Student = studentService!!.findOne(student.id!!)!!

        LessonIdsResponseAssert(missResponse)
                .error("common.error.lessons.change.lesson.status.illegal.status", arrayOf(StudentSlotId(lessonIdToPlan, modifiedStudent).id, StudentSlotStatus.missed))
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
                .isEmpty()
    }
}