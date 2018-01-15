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
import com.joins.kidcenter.db.DbSequenceHelper
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.dto.StudentDashboardLesson
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.*
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.service.lessons.LessonTemplateService
import com.joins.kidcenter.service.lessons.StudentSlotService
import com.joins.kidcenter.service.maps.StudentNextLessonsStorage
import com.joins.kidcenter.service.sms.scheduling.SmsSchedulingService
import com.joins.kidcenter.util.TestUtils
import com.joins.kidcenter.utils.DateTimeUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(CommonConfiguration::class, IntgTestConfiguration::class))
@ActiveProfiles(profiles = arrayOf("test"))
open class LessonsPlanTest {
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
    fun testPlanSingleLessonInPast() {
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

        PlanLessonResponseAssert(planResponse).persisted(true)
                .plannedLessonIds(lessonIdToPlan.id())
                .skippedLessons()
                .studentPlannedLessons()
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
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .isEmpty()
    }

    @Test
    fun testPlanSingleLessonInFuture() {
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

        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, card.id!!, lessonIds = arrayOf(lessonIdToPlan.id()))

        val planResponse = lessonSlotService!!.planLessons(planRequest)

        PlanLessonResponseAssert(planResponse).persisted(true)
                .plannedLessonIds(lessonIdToPlan.id())
                .skippedLessons()
                .studentPlannedLessons(StudentDashboardLesson(0, VisitType.regular, 1, StudentSlotStatus.planned, null, false, lessonIdToPlan.id(), LessonAgeGroup.g2_3, DateTimeUtils.currentDateTime(), "system"))
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
    fun testPlanRepeatedLessonsStartingFromPast() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(LessonDay.tuesday, 11, 10, LessonSubject.art, LessonAgeGroup.g3_5)
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

        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInPast(LessonDay.tuesday, 11, 10, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, card.id!!, true, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))

        val planResponse = lessonSlotService!!.planLessons(planRequest)

        val expectedPlannedLessonSlots = factories!!.lesson.repeatedLessonSlots(lessonIdToPlan1, 10)
                .plus(factories!!.lesson.repeatedLessonSlots(lessonIdToPlan2, 10))
        val expectedStudentDashboardLessons = factories!!.lesson.repeatedStudentDashboardLesson(lessonIdToPlan1.plusWeeks(), VisitType.regular, LessonAgeGroup.g2_3, StudentSlotStatus.planned, null, false, 9)
                .plus(factories!!.lesson.repeatedStudentDashboardLesson(lessonIdToPlan2.plusWeeks(), VisitType.regular, LessonAgeGroup.g3_5, StudentSlotStatus.planned, null, false, 9))
        PlanLessonResponseAssert(planResponse).persisted(true)
                .plannedLessonSlotIds(*expectedPlannedLessonSlots.toTypedArray())
                .skippedLessons()
                .studentPlannedLessons(*expectedStudentDashboardLessons.toTypedArray())
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 0)
                .cancels(5, 5)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(20)
                .visitedLessonsCount(0)
                .usedLessonsCount(20)
                .availableLessonsCount(0)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(lessonIdToPlan1.plusWeeks(1).andFollowingWeeks(1).plus(lessonIdToPlan2.plusWeeks(1).andFollowingWeeks(1)))
    }

    @Test
    fun testPlanUnknownStudent() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(-1, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.student.not.found") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    @Test
    fun testPlanUnknownStudentCard() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val lessonIdToPlan = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planRequest = PlanLessonsRequest(student.id!!, -1, false, lessonIds = arrayOf(lessonIdToPlan.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.student.card.not.found") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    @Test
    fun testPlanNoSelectedLessons() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf())

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.no.lessons.selected") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    @Test
    fun testPlanLessonsNotAvailable() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular().apply { lessonsLimit = 1 }).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInPast(LessonDay.tuesday, 11, 10, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.lessons.not.available") })
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(1, 1)
                .cancels(5, 5)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(1)
                .bonusLessonsCount(0)
                .plannedLessonsCount(0)
                .visitedLessonsCount(0)
                .usedLessonsCount(0)
                .availableLessonsCount(1)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
    }

    @Test
    fun testPlanLessonsTimeSubjectConflict() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.art)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.time.subject.conflict") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    @Test
    fun testPlanAfterExpirationDate() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular().apply { durationDays = 1 }).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(LessonDay.tuesday, 11, 10, LessonSubject.art, LessonAgeGroup.g2_3).asSaveRequest())
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(LessonDay.tuesday, 11, 10, LessonSubject.fitness).plusWeeks(1)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.after.card.expiration.date") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    @Test
    fun testPlanBoyToBallet() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInPast(LessonDay.monday, 9, 0, LessonSubject.ballet)
        val planRequest = PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan1.id()))

        TestUtils.assertException(
                { lessonSlotService!!.planLessons(planRequest) },
                { e -> PlanLessonExceptionAssert(e).text("common.error.lessons.plan.assign.boy.for.ballet") })
        assertSampleRegularUnusedCard(studentCard)
        assertStudentWithSampleRegularUnusedCard(student, studentCard)
    }

    private fun assertSampleRegularUnusedCard(studentCard: SaveResult<StudentCard>) {
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .suspends(5, 5)
    }

    private fun assertStudentWithSampleRegularUnusedCard(student: Student, studentCard: SaveResult<StudentCard>) {
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
    }
}