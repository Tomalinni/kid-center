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

package com.joins.kidcenter.service.sms.scheduling

import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.repository.LessonSlotRepository
import com.joins.kidcenter.service.StudentCardService
import com.joins.kidcenter.service.StudentService
import com.joins.kidcenter.service.lessons.LessonEvent
import com.joins.kidcenter.service.lessons.LessonEventType
import com.joins.kidcenter.service.sms.SmsPersonalizedMessage
import com.joins.kidcenter.service.sms.senders.SmsSender
import com.joins.kidcenter.service.templates.LessonsListTemplateDescriptor
import com.joins.kidcenter.service.templates.TemplateRenderer
import com.joins.kidcenter.utils.DateTimeUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import javax.annotation.PostConstruct

interface SmsSchedulingService {
    fun sendNextDaysLessonNotifications()
    fun registerLessonEvent(event: LessonEvent)
    fun getScheduledNotifications(): Map<Long, StudentLessonNotification>
    /**
     * Use in tests only
     */
    fun cancelScheduledNotifications()
}

@Service
@Transactional
open class SmsSchedulingServiceImpl @Autowired constructor(
        val templateRenderer: TemplateRenderer,
        val smsSender: SmsSender,
        val lessonSlotRepository: LessonSlotRepository,
        val studentService: StudentService,
        val studentCardService: StudentCardService
) : SmsSchedulingService {

    private val log = LoggerFactory.getLogger(SmsSchedulingService::class.java)!!

    private val tasks: MutableMap<Long, LessonsUpdatedNotificationTaskDescriptor> = ConcurrentHashMap()
    private val taskScheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @PostConstruct
    private fun init() {
        taskScheduler.schedule(CurrentDayPlannedLessonsNotificationTask(), LessonTimeTrigger())
    }

    @Scheduled(cron = "0 0 19 * * *")
    override fun sendNextDaysLessonNotifications() {
        val today = DateTimeUtils.currentDate()
        if (today.dayOfWeek == DayOfWeek.MONDAY) {
            val startDateTime = today.plusDays(1).atStartOfDay()
            val endDateTime = today.plusWeeks(1).atTime(LocalTime.MAX)
            val lessonSlots = lessonSlotsForDates(startDateTime, endDateTime)
            val studentNotifications: MutableMap<Long, StudentLessonNotification> = createStudentLessonNotifications(listOf(), lessonSlots, null)
            sendPlannedLessonsNotification(studentNotifications.values, TimeScope.week)
        } else {
            val tomorrow = today.plusDays(1)
            val startDateTime = tomorrow.atStartOfDay()
            val endDateTime = tomorrow.atTime(LocalTime.MAX)
            val lessonSlots = lessonSlotsForDates(startDateTime, endDateTime)
            val studentNotifications: MutableMap<Long, StudentLessonNotification> = createStudentLessonNotifications(listOf(), lessonSlots, null)
            sendPlannedLessonsNotification(studentNotifications.values, TimeScope.nextDay)
        }
    }

    private fun sendCurrentDayLessonNotifications() {
        val startDateTime = DateTimeUtils.currentDateTime()
        val endDateTime = startDateTime.toLocalDate().atTime(LocalTime.MAX)
        val lessonSlots = lessonSlotsForDates(startDateTime, endDateTime)
        val currentFirstLessonOwners = findCurrentFirstLessonOwners(startDateTime)
        val studentNotifications: MutableMap<Long, StudentLessonNotification> = createStudentLessonNotifications(listOf(), lessonSlots, currentFirstLessonOwners)
        sendPlannedLessonsNotification(studentNotifications.values, TimeScope.currentDay)
    }

    private fun createStudentLessonNotifications(recentlyChangedLessonSlots: List<LessonSlot>, plannedLessonSlots: List<LessonSlot>, notifiedStudentIds: Collection<Long>?): MutableMap<Long, StudentLessonNotification> {
        val studentNotifications: MutableMap<Long, StudentLessonNotification> = mutableMapOf()

        fillStudentNotifications(recentlyChangedLessonSlots, notifiedStudentIds, studentNotifications, { it.recentlyChangedLessons })
        fillStudentNotifications(plannedLessonSlots, notifiedStudentIds, studentNotifications, { it.plannedLessons })
        return studentNotifications
    }

    private fun fillStudentNotifications(lessonSlots: List<LessonSlot>, notifiedStudentIds: Collection<Long>?, studentNotifications: MutableMap<Long, StudentLessonNotification>, lessonListAccessor: (StudentLessonNotification) -> NotificationLessonList) {
        lessonSlots.forEach { lessonSlot ->
            val lessonDateTime = lessonSlot.dateTime
            val lessonTime = lessonDateTime.format(timeFormatter)
            val subject = lessonSlot.subject.translated()

            lessonSlot.students.forEach { studentSlot ->
                val isPlanned = studentSlot.status == StudentSlotStatus.planned && studentSlot.student != null
                val student = studentSlot.student!!
                val matchesSpecifiedStudent = notifiedStudentIds == null || notifiedStudentIds.contains(student.id)
                if (isPlanned && matchesSpecifiedStudent) {

                    val studentId = student.id!!
                    var notification = studentNotifications[studentId]
                    if (notification == null) {
                        notification = StudentLessonNotification(student)
                        studentNotifications.put(studentId, notification)
                    }

                    val lessonDate = lessonDateTime.format(dateFormatter)
                    var day = lessonListAccessor(notification).days[lessonDate]
                    if (day == null) {
                        day = DayUpdateInfo(lessonDate, "${lessonDateTime.monthValue}", "${lessonDateTime.dayOfMonth}", LessonDay.fromWeekDay(lessonDateTime.dayOfWeek).dayName())
                        lessonListAccessor(notification).days.put(lessonDate, day)
                    }
                    day.lessons.add(LessonUpdateInfo(lessonTime, subject, null))


                    val relatives = studentService.findRelativesWithMobileNotification(studentId, MobileNotification.plannedLesson)
                    relatives.forEach {
                        notification!!.mobilesToRelativeNames.put(it.mobile!!, it.name)
                    }
                }
            }
        }
    }

    override fun registerLessonEvent(event: LessonEvent) {

        when (event.type) {
            LessonEventType.plan -> {
                val student = studentCardService.findStudentByCard(event.studentCardId) ?: return
                var taskDescriptor: LessonsUpdatedNotificationTaskDescriptor?

                synchronized(tasks) {
                    val studentId = student.id!!
                    taskDescriptor = tasks[studentId]

                    if (taskDescriptor != null) {
                        taskDescriptor!!.future.cancel(false)
                    }

                    val startDateTime = DateTimeUtils.currentDateTime()
                    val endDateTime = startDateTime.toLocalDate().plusDays(Config.lessonsChangeLookAheadDays.toLong()).atTime(LocalTime.MAX)
                    val weekPlannedLessonSlots = lessonSlotsForStudentAndDates(studentId, startDateTime, endDateTime)
                    val recentlyPlannedLessonSlots = lessonSlotRepository.findAll(event.lessonSlotIds).toList()
                    val firstRecentlyPlannedLessonSlots = recentlyPlannedLessonSlots.subList(0, Math.min(recentlyPlannedLessonSlots.size, 5))
                    val studentNotifications: MutableMap<Long, StudentLessonNotification> = createStudentLessonNotifications(firstRecentlyPlannedLessonSlots, weekPlannedLessonSlots, listOf(studentId))
                    val notification = studentNotifications[studentId]

                    if (notification != null) {
                        val future = taskScheduler.schedule(LessonsUpdatedNotificationTask(studentId, notification), getMessageSendTime())
                        taskDescriptor = LessonsUpdatedNotificationTaskDescriptor(future, notification)
                        tasks.put(studentId, taskDescriptor!!)
                    }

                }
            }
            else -> return
        }
    }

    override fun getScheduledNotifications(): Map<Long, StudentLessonNotification> {
        synchronized(tasks) {
            return tasks.entries.map { Pair(it.key, it.value.notification) }.toMap()
        }
    }

    /**
     * Use in tests only
     */
    override fun cancelScheduledNotifications() {
        synchronized(tasks) {
            tasks.values.forEach { it.future.cancel(true) }
            tasks.clear()
        }
    }

    private fun getMessageSendTime(): Date {
        val delayMs = (Config.lessonsChangeNotificationSendDelayMins * 60 * 1000).toLong()
        return Date(System.currentTimeMillis() + delayMs)
    }

    private fun findCurrentFirstLessonOwners(dateTime: LocalDateTime): Collection<Long> {
        val futureLessonOwners: Set<Long> = lessonSlotRepository.findStudentIdsForDatesAndStatus(dateTime, dateTime.plusHours(1).plusMinutes(5 /*ensure lesson will be started*/), StudentSlotStatus.planned.ordinal).map({ it.toLong() }).toSet()
        val pastLessonOwners: Set<Long> = lessonSlotRepository.findStudentIdsForDatesAndStatus(dateTime.toLocalDate().atStartOfDay(), dateTime, StudentSlotStatus.planned.ordinal).map({ it.toLong() }).toSet()
        return futureLessonOwners.minus(pastLessonOwners)
    }

    private fun lessonSlotsForDates(startDate: LocalDateTime, endDate: LocalDateTime): List<LessonSlot> {
        return lessonSlotRepository.findForDates(startDate, endDate).filter { it.students.isNotEmpty() }
    }

    private fun lessonSlotsForStudentAndDates(studentId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<LessonSlot> {
        return lessonSlotRepository.findForStudentDatesAndStatus(studentId, startDate, endDate, StudentSlotStatus.planned.ordinal)
    }

    private fun sendPlannedLessonsNotification(messages: Collection<StudentLessonNotification>, timeScope: TimeScope) {
        smsSender.sendPlannedLessonNotifications(generatePlannedLessonsMessages(messages.filter { it.plannedLessons.days.isNotEmpty() }), timeScope)
    }

    private fun sendChangedLessonsNotification(messages: Collection<StudentLessonNotification>) {
        smsSender.sendChangedLessonNotifications((generateChangedLessonsMessages(messages.filter { it.recentlyChangedLessons.days.isNotEmpty() })))
    }

    private fun generatePlannedLessonsMessages(notifications: Collection<StudentLessonNotification>): Collection<SmsPersonalizedMessage> {
        val messages: MutableMap<String, SmsPersonalizedMessage> = mutableMapOf()
        notifications.forEach {
            for ((mobile, relativeName) in it.mobilesToRelativeNames) {
                val student = it.student
                val studentBusinessIdArgument = student.businessId
                val studentNameArgument = student.nameCn ?: student.nameEn ?: "-"
                val contactPhoneArgument = if (student.status == StudentStatus.cardPaid) Config.paidSupportContactNumber else Config.schoolContactNumber
                val lessonListArgument = LessonsListTemplateDescriptor(it.plannedLessons).render(templateRenderer)
                val arguments = listOf(Config.schoolNameEn, studentBusinessIdArgument, studentNameArgument, lessonListArgument, contactPhoneArgument)
                messages.put(mobile, SmsPersonalizedMessage(mobile, arguments))
            }
        }
        return messages.values
    }

    private fun generateChangedLessonsMessages(notifications: Collection<StudentLessonNotification>): Collection<SmsPersonalizedMessage> {
        val messages: MutableMap<String, SmsPersonalizedMessage> = mutableMapOf()
        notifications.forEach {
            for ((mobile, relativeName) in it.mobilesToRelativeNames) {
                val student = it.student
                val studentBusinessIdArgument = student.businessId
                val studentNameArgument = student.nameCn ?: student.nameEn ?: "-"
                val contactPhoneArgument = if (student.status == StudentStatus.cardPaid) Config.paidSupportContactNumber else Config.schoolContactNumber
                val recentlyChangedLessonsArgument = LessonsListTemplateDescriptor(it.recentlyChangedLessons).render(templateRenderer)
                val plannedLessonsArgument = LessonsListTemplateDescriptor(it.plannedLessons).render(templateRenderer)
                val arguments = listOf(Config.schoolNameEn, studentBusinessIdArgument, studentNameArgument, recentlyChangedLessonsArgument, plannedLessonsArgument, contactPhoneArgument)
                messages.put(mobile, SmsPersonalizedMessage(mobile, arguments))
            }
        }
        return messages.values
    }

    inner class LessonsUpdatedNotificationTask(
            val studentId: Long,
            val notification: StudentLessonNotification
    ) : Runnable {
        override fun run() {
            val future = tasks[studentId]

            synchronized(future!!) {
                sendChangedLessonsNotification(listOf(notification))
            }
        }
    }

    inner class CurrentDayPlannedLessonsNotificationTask : Runnable {
        override fun run() {
            sendCurrentDayLessonNotifications()
        }
    }
}

class LessonsUpdatedNotificationTaskDescriptor(val future: ScheduledFuture<*>,
                                               val notification: StudentLessonNotification)


data class StudentLessonNotification(
        val student: Student,
        val mobilesToRelativeNames: MutableMap<String, String?> = mutableMapOf(),
        val recentlyChangedLessons: NotificationLessonList = NotificationLessonList(),
        val plannedLessons: NotificationLessonList = NotificationLessonList()
)

data class NotificationLessonList(
        val days: MutableMap<String, DayUpdateInfo> = mutableMapOf()
)

data class DayUpdateInfo(
        val date: String,
        val month: String,
        val monthDay: String,
        val weekDay: String,
        val lessons: MutableList<LessonUpdateInfo> = mutableListOf()
)

data class LessonUpdateInfo(
        val time: String,
        val subject: String,
        val type: String?
)
