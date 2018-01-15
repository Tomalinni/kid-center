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
import com.joins.kidcenter.controller.LessonSlotRequest
import com.joins.kidcenter.controller.PlanLessonsRequest
import com.joins.kidcenter.controller.RepeatedStudentSlotRequest
import com.joins.kidcenter.controller.StudentSlotRequest
import com.joins.kidcenter.db.DbSequenceHelper
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.dto.lessons.LessonSlotId
import com.joins.kidcenter.dto.lessons.LessonSlotIds
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.LessonIdsResponseAssert
import com.joins.kidcenter.helper.assert.StudentAssert
import com.joins.kidcenter.helper.assert.StudentCardAssert
import com.joins.kidcenter.helper.assert.StudentScheduledNotificationsAssert
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
open class LessonsUnplanTest {
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
    fun testUnplanSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(LessonDay.tuesday, 11, 10, LessonSubject.art, LessonAgeGroup.g3_5)
                .asSaveRequest())
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(LessonDay.tuesday, 11, 10, LessonSubject.art)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id())))

        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(planResponse.plannedLessonIds[lessonIdToPlan1.id()]!!))
        val expectedPlannedStudentDashboardLessons = factories!!.lesson.repeatedStudentDashboardLesson(lessonIdToPlan2, VisitType.regular, LessonAgeGroup.g3_5, StudentSlotStatus.planned, null, false, 1)
        LessonIdsResponseAssert(unplanResponse)
                .success()
                .studentPlannedLessons(expectedPlannedStudentDashboardLessons)
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
                .nextLessons(listOf(lessonIdToPlan2.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!)) //notification is not created for lesson planned in past
    }

    @Test
    fun testUnplanCanceledSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(3, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())
        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(3, 0, 0, LessonSubject.fitness)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan.id())))
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(planResponse.plannedLessonIds[lessonIdToPlan.id()]!!))
        LessonIdsResponseAssert(unplanResponse).success()
        assertAfterUnplanState(student, studentCard, lessonIdToPlan)
    }

    @Test
    fun testUnplanCanceledInvalidatedSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular().apply { lateCancelsLimit = 0 }).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(0, 2, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())
        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(0, 2, 0, LessonSubject.fitness)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan.id())))
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(studentSlotId!!, true))

        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(planResponse.plannedLessonIds[lessonIdToPlan.id()]!!))
        LessonIdsResponseAssert(unplanResponse).success()
        assertNull(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!))
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
                .cancels(5, 5)
                .lateCancels(0, 0)
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
                .forStudents(listOf()) //notification is not created for lesson planned in past
    }

    @Test
    fun testUnplanVisitedSingleLesson() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlotInFuture(3, 0, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())
        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(3, 0, 0, LessonSubject.fitness)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, false, lessonIds = arrayOf(lessonIdToPlan.id())))
        val studentSlotId = planResponse.plannedLessonIds[lessonIdToPlan.id()]
        assertNotNull(studentSlotId)
        lessonSlotService!!.visitLesson(RepeatedStudentSlotRequest(studentSlotId!!))

        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(planResponse.plannedLessonIds[lessonIdToPlan.id()]!!))
        LessonIdsResponseAssert(unplanResponse).success()
        assertAfterUnplanState(student, studentCard, lessonIdToPlan)
    }

    @Test
    fun testUnplanRepeatedLessons() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(LessonDay.tuesday, 11, 10, LessonSubject.art, LessonAgeGroup.g3_5)
                .asSaveRequest())
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val lessonIdToPlan2 = factories!!.lesson.lessonSlotInFuture(LessonDay.tuesday, 11, 10, LessonSubject.art)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, true, lessonIds = arrayOf(lessonIdToPlan1.id(), lessonIdToPlan2.id())))

        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(planResponse.plannedLessonIds[lessonIdToPlan1.plusWeeks().id()]!!))
        val expectedPlannedStudentDashboardLessons = factories!!.lesson.repeatedStudentDashboardLesson(lessonIdToPlan1, VisitType.regular, LessonAgeGroup.g2_3, StudentSlotStatus.planned, null, false, 1)
                .plus(factories!!.lesson.repeatedStudentDashboardLesson(lessonIdToPlan2, VisitType.regular, LessonAgeGroup.g3_5, StudentSlotStatus.planned, null, false, 10))
        LessonIdsResponseAssert(unplanResponse)
                .success()
                .studentPlannedLessons(expectedPlannedStudentDashboardLessons)
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 9)
                .cancels(5, 5)
                .lateCancels(1, 1)
                .lastMomentCancels(1, 1)
                .undueCancels(1, 1)
                .suspends(5, 5)
        StudentAssert(studentService!!.findOne(student.id!!)!!)
                .status(StudentStatus.cardPaid)
                .paidLessonsCount(20)
                .bonusLessonsCount(0)
                .plannedLessonsCount(11)
                .visitedLessonsCount(0)
                .usedLessonsCount(11)
                .availableLessonsCount(9)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(lessonIdToPlan2.andFollowingWeeks(2).plus(LessonSlotIds(listOf(lessonIdToPlan1))))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!)) //notification is not created for lesson planned in past
    }

    @Test
    fun testUnplanRepeatedLessonsInDifferentStatuses() {
        val student = studentService!!.save(factories!!.student.student()).obj!!
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        val studentCard = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student, card))
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .addSlot(LessonDay.tuesday, 11, 10, LessonSubject.art, LessonAgeGroup.g3_5)
                .asSaveRequest())
        val lessonIdToPlan1 = factories!!.lesson.lessonSlotInFuture(LessonDay.monday, 9, 0, LessonSubject.fitness)
        val planResponse = lessonSlotService!!.planLessons(PlanLessonsRequest(student.id!!, studentCard.obj!!.id!!, true, lessonIds = arrayOf(lessonIdToPlan1.id())))
        val slotId1 = planResponse.plannedLessonIds[lessonIdToPlan1.plusWeeks(1).id()]!!
        val slotId2 = planResponse.plannedLessonIds[lessonIdToPlan1.plusWeeks(2).id()]!!
        val slotId3 = planResponse.plannedLessonIds[lessonIdToPlan1.plusWeeks(3).id()]!!
        lessonSlotService!!.visitLesson(StudentSlotRequest(slotId1))
        lessonSlotService!!.missLesson(StudentSlotRequest(slotId2))
        lessonSlotService!!.cancelLesson(RepeatedStudentSlotRequest(slotId3))
        lessonSlotService!!.revokeLesson(LessonSlotRequest(lessonIdToPlan1.plusWeeks(4).id()))


        val unplanResponse = lessonSlotService!!.unplanLesson(RepeatedStudentSlotRequest(slotId1))
        LessonIdsResponseAssert(unplanResponse)
                .success()
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
                .nextLessons(listOf(lessonIdToPlan1.id()))
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student.id!!)) //notification is not created for lesson planned in past
    }

    private fun assertAfterUnplanState(student: Student, studentCard: SaveResult<StudentCard>, lessonIdToPlan: LessonSlotId) {
        assertNull(studentSlotService!!.findByLessonAndCard(lessonIdToPlan, studentCard.obj!!.id!!))
        StudentCardAssert(studentCardService!!.findOne(studentCard.obj!!.id!!)!!)
                .activated(DateTimeUtils.currentDate())
                .lessons(20, 20)
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
                .usedLessonsCount(0)
                .availableLessonsCount(20)
                .lastCardValidDate(DateTimeUtils.currentDate().plusDays(studentCard.obj!!.durationDays.toLong()))
                .nextLessons(listOf())
        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf()) //notification is not created for lesson planned in past
    }
}