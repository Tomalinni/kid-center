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

package com.joins.kidcenter.controller

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.dto.FileNames
import com.joins.kidcenter.dto.LessonsDataDto
import com.joins.kidcenter.dto.StudentDashboardLesson
import com.joins.kidcenter.dto.internal.OperationResult
import com.joins.kidcenter.dto.internal.ParametrizedMessage
import com.joins.kidcenter.dto.lessons.SlotStatus
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.service.lessons.LessonTemplateService
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/lessons")
class LessonsController @Autowired constructor(
        val objectMapper: ObjectMapper,
        val lessonTemplateService: LessonTemplateService,
        val lessonSlotService: LessonSlotService,
        val storageService: FileStorageServiceImpl) {

    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findData(response: HttpServletResponse, @RequestParam parameters: Map<String, String>?) {
        SecurityUtil.checkPermission(Permission.lessonsRead)
        val lessonsRequest = LessonsDataRequest.fromMap(parameters)
        val lessons = LessonsDataDto()
        val dbTemplates = lessonTemplateService.forDates(lessonsRequest.startDate, lessonsRequest.endDate)
        dbTemplates.forEach { lessons.addTemplate(it) }

        val startDateTime = lessonsRequest.startDate.toLocalDateTimeMidnight()
        val endDateTime = lessonsRequest.endDate.toLocalDateTimeMidnight()
        val lessonSlots = lessonSlotService.lessonSlotsForDates(startDateTime, endDateTime)
        lessonSlots.forEach { lessons.addLessonPlan(it) }

        lessons.students = lessonSlotService.studentsForDataRequest(startDateTime, endDateTime, lessonsRequest.studentId).map { student ->
            Pair(student.id, student)
        }.toMap()

        response.sendObject(objectMapper, lessons)
    }

    @RequestMapping("/plan", method = arrayOf(RequestMethod.POST))
    fun planLessons(response: HttpServletResponse, @RequestBody request: PlanLessonsRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.planLessons(request))
    }

    @RequestMapping("/visit", method = arrayOf(RequestMethod.POST))
    fun visitLesson(response: HttpServletResponse, @RequestBody request: StudentSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsClose)
        response.sendObject(objectMapper, lessonSlotService.visitLesson(request))
    }

    @RequestMapping("/miss", method = arrayOf(RequestMethod.POST))
    fun missLesson(response: HttpServletResponse, @RequestBody request: StudentSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsClose)
        response.sendObject(objectMapper, lessonSlotService.missLesson(request))
    }

    @RequestMapping("/cancel", method = arrayOf(RequestMethod.POST))
    fun cancelLesson(response: HttpServletResponse, @RequestBody request: RepeatedStudentSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.cancelLesson(request))
    }

    @RequestMapping("/revoke", method = arrayOf(RequestMethod.POST))
    fun revokeLesson(response: HttpServletResponse, @RequestBody request: LessonSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsRevoke)
        response.sendObject(objectMapper, lessonSlotService.revokeLesson(request))
    }

    @RequestMapping("/close", method = arrayOf(RequestMethod.POST))
    fun closeLesson(response: HttpServletResponse, @RequestBody request: LessonSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsClose)
        response.sendObject(objectMapper, lessonSlotService.closeLesson(request))
    }

    @RequestMapping("/suspend", method = arrayOf(RequestMethod.POST))
    fun suspendLessons(response: HttpServletResponse, @RequestBody request: SuspendLessonsRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.suspendLessons(request))
    }

    @RequestMapping("/unplan", method = arrayOf(RequestMethod.POST))
    fun unplanLesson(response: HttpServletResponse, @RequestBody request: RepeatedStudentSlotRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.unplanLesson(request))
    }

    @RequestMapping("/transfer", method = arrayOf(RequestMethod.POST))
    fun transferLesson(response: HttpServletResponse, @RequestBody request: TransferLessonsRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.transferLessons(request))
    }

    @RequestMapping(path = arrayOf("/{lessonId}/photos"), method = arrayOf(RequestMethod.POST))
    fun uploadLessonPhoto(response: HttpServletResponse, @PathVariable lessonId: String, @RequestParam(name = "photo") photos: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.lessonsModify)
        val lesson = lessonSlotService.findOne(lessonId)
        if (lesson != null) {
            val storageProvider = storageService.providers().lesson(lessonId)
            val newPhotoNames = PhotoNames.unusedPhotoNames(storageProvider.listNames(), photos.map { it.originalFilename })
            photos.forEachIndexed { i, file -> storageProvider.upload(newPhotoNames[i], file.inputStream) }
            response.sendObject(objectMapper, FileNames(newPhotoNames))
        }
    }

    @RequestMapping(path = arrayOf("/{lessonId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadLessonPhoto(response: HttpServletResponse, @PathVariable lessonId: String, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.lessonsRead)
        val lesson = lessonSlotService.findOne(lessonId)
        val storageProvider = storageService.providers().lesson(lessonId)
        if (lesson != null && storageProvider.exists(photoName)) {
            storageProvider.download(photoName, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{lessonId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deleteLessonPhoto(response: HttpServletResponse, @PathVariable lessonId: String, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.lessonsModify)
        val lesson = lessonSlotService.findOne(lessonId)
        if (lesson != null) {
            val storageProvider = storageService.providers().lesson(lessonId)
            storageProvider.delete(photoName)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{lessonId}/photos/"), method = arrayOf(RequestMethod.GET))
    fun findPhotos(response: HttpServletResponse, @PathVariable lessonId: String) {
        val photos = storageService.providers().lesson(lessonId).listNames()
        response.sendObject(objectMapper, FileNames(photos))
    }

    @RequestMapping("/setPresenceInSchool", method = arrayOf(RequestMethod.PUT))
    fun setPresenceInSchool(response: HttpServletResponse, @RequestBody request: SetPresenceInSchoolRequest) {
        SecurityUtil.checkPermission(Permission.lessonsPlan)
        response.sendObject(objectMapper, lessonSlotService.setPresenceInSchool(request))
    }

    @RequestMapping("/planned/student/{studentId}", method = arrayOf(RequestMethod.GET))
    fun findPlannedLessonsByStudent(response: HttpServletResponse, @PathVariable studentId: Long) {
        SecurityUtil.checkPermission(Permission.lessonsRead)
        response.sendObject(objectMapper, lessonSlotService.findPlannedLessonsByStudent(studentId))
    }
}

class LessonsDataRequest(
        var startDate: LocalDate,
        var endDate: LocalDate,
        var studentId: Long?,
        var entities: Collection<String>
) {
    companion object {

        fun fromMap(parameters: Map<String, String>?): LessonsDataRequest {
            val request = defaultObject()
            if (parameters !== null) {
                request.startDate = parameters["startDate"]?.toLocalDate(request.startDate) ?: request.startDate
                request.endDate = parameters["endDate"]?.toLocalDate(request.endDate) ?: request.endDate
                request.studentId = parameters["studentId"].toLongOrNull()
                request.entities = parameters["entities"]?.split("\\s*,\\s*") ?: emptySet<String>()
            }
            return request
        }

        private fun defaultObject(): LessonsDataRequest {
            val now = DateTimeUtils.currentDate()
            return LessonsDataRequest(now, now.plusDays(21), null, emptySet())
        }
    }
}

class PlanLessonsRequest(
        var studentId: Long = 0,
        var cardId: Long = 0,
        var repeatWeekly: Boolean = false,
        var lessonProcedure: LessonProcedure = LessonProcedure.plan,
        var lessonIds: Array<String> = arrayOf()
)

enum class LessonProcedure {
    plan
}

class TransferLessonsRequest(
        var studentId: Long = 0,
        var cardId: Long = 0,
        var repeatWeekly: Boolean = false,
        var lessonIds: List<String> = listOf(),
        var transferCardId: Long = 0,
        var targetStudentId: Long = 0
)

class SuspendLessonsRequest(
        var cardId: Long = 0,
        var fromDate: LocalDate = DateTimeUtils.currentDate().plusDays(1)
)

class PlanLessonsResponse(
        var persisted: Boolean = false,
        //map<lessonId, slotId>
        var plannedLessonIds: Map<String, Long> = emptyMap(),
        //map<lessonId, errorMessage>
        var skippedLessons: Map<String, ParametrizedMessage> = emptyMap(),
        var studentPlannedLessons: List<StudentDashboardLesson> = listOf(),
        @JsonUnwrapped
        val response: OperationResponse = OperationResponse.success()) {

    companion object {
        fun fromOperationResult(result: OperationResult<out Any>): PlanLessonsResponse {
            val messages = result.errors
            if (messages.isEmpty()) {
                return PlanLessonsResponse(response = OperationResponse.success()) //success
            } else {
                return PlanLessonsResponse(response = OperationResponse.error(messages[0]))
            }
        }
    }
}

class LessonSlotRequest(
        var lessonId: String = ""
)

open class StudentSlotRequest(val slotId: Long,
                              val confirmed: Boolean = false)

class RepeatedStudentSlotRequest(slotId: Long,
                                 confirmed: Boolean = false,
                                 val repeatWeekly: Boolean = false) : StudentSlotRequest(slotId, confirmed)

class SetPresenceInSchoolRequest(
        var studentId: Long = 0,
        var presentInSchool: Boolean = false
)

class UnplanLessonResponse {
    var unplannedStudentSlots: List<SlotStatus> = listOf()
    var studentPlannedLessons: List<StudentDashboardLesson> = listOf()
}

class LessonIdsResponse(@JsonUnwrapped
                        val response: OperationResponse,
                        val studentPlannedLessons: List<StudentDashboardLesson> = listOf()) {

    constructor(message: String, vararg params: Any) : this(OperationResponse.error(message, *params))

    constructor(studentPlannedLessons: List<StudentDashboardLesson>) : this(OperationResponse.success(), studentPlannedLessons)
}

open class OperationResponse private constructor(
        val error: ParametrizedMessage? = null,
        val warning: ParametrizedMessage? = null) {

    fun success(): Boolean = error == null && warning == null

    companion object {
        fun success(): OperationResponse {
            return OperationResponse()
        }

        fun error(message: String, vararg params: Any): OperationResponse {
            return error(ParametrizedMessage(message, *params))
        }

        fun error(message: ParametrizedMessage): OperationResponse {
            return OperationResponse(message)
        }

        fun warning(message: String, vararg params: Any): OperationResponse {
            return warning(ParametrizedMessage(message, *params))
        }

        fun warning(message: ParametrizedMessage): OperationResponse {
            return OperationResponse(null, message)
        }

        fun fromOperationResult(result: OperationResult<out Any>): OperationResponse {
            val messages = result.errors
            if (messages.isEmpty()) {
                return OperationResponse() //success
            } else {
                return OperationResponse(messages[0])
            }
        }
    }
}

open class EntityResponse<out T>(
        val obj: T?,
        @JsonUnwrapped
        val response: OperationResponse = OperationResponse.success()) {
    fun success(): Boolean = response.success()
}

