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
import com.joins.kidcenter.domain.LessonAgeGroup
import com.joins.kidcenter.domain.LessonDay
import com.joins.kidcenter.domain.LessonSubject
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.StudentScheduledNotificationsAssert
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.service.lessons.LessonTemplateService
import com.joins.kidcenter.service.maps.StudentNextLessonsStorage
import com.joins.kidcenter.service.sms.scheduling.SmsSchedulingService
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
open class StudentScheduledNotificationsTest {
    @Autowired
    var studentService: StudentService? = null
    @Autowired
    var cardService: CardService? = null
    @Autowired
    var studentCardService: StudentCardService? = null
    @Autowired
    var lessonSlotService: LessonSlotService? = null
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
    fun testSchedukedNotificationsCreatedIndividually() {
        val card = cardService!!.save(factories!!.card.regular()).obj!!
        lessonTemplateService!!.save(factories!!.lesson.lessonTemplate()
                .addSlot(LessonDay.monday, 9, 0, LessonSubject.fitness, LessonAgeGroup.g2_3)
                .asSaveRequest())
        val lessonIdToPlan = factories!!.lesson.lessonSlotInFuture(LessonDay.monday, 9, 0, LessonSubject.fitness)

        val student1 = studentService!!.save(factories!!.student.student()).obj!!
        val studentCard1 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student1, card))
        lessonSlotService!!.planLessons(PlanLessonsRequest(student1.id!!, studentCard1.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id())))

        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student1.id!!))

        smsSchedulingService!!.cancelScheduledNotifications()

        val student2 = studentService!!.save(factories!!.student.student()).obj!!
        val studentCard2 = studentCardService!!.save(factories!!.studentCard.fromStudentAndCard(student2, card))
        lessonSlotService!!.planLessons(PlanLessonsRequest(student2.id!!, studentCard2.obj!!.id!!, lessonIds = arrayOf(lessonIdToPlan.id())))

        StudentScheduledNotificationsAssert(smsSchedulingService!!.getScheduledNotifications())
                .forStudents(listOf(student2.id!!))
    }
}