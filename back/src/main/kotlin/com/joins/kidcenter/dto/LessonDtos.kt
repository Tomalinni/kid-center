package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.utils.DateTimeUtils
import java.time.LocalDate

class LessonsDataDto() {
    val templates: MutableMap<Long, LessonTemplateDto> = mutableMapOf()
    val lessonPlans: MutableMap<String, LessonSlotDto> = mutableMapOf()
    var students: Map<Long, LessonStudentDto> = mapOf()

    fun addTemplate(template: LessonTemplateDto): LessonsDataDto {
        templates[template.id] = template
        return this
    }

    fun addLessonPlan(lessonSlot: LessonSlotDto): LessonsDataDto {
        lessonPlans[lessonSlot.id] = lessonSlot
        return this
    }
}

class LessonTemplateDto() {
    var id: Long = 0
    var name: String = "Template is not defined"
    var startDate: LocalDate = LessonTemplate.minStartDate
    var endDate: LocalDate = LessonTemplate.maxEndDate

    val lessons: MutableMap<LessonDay, MutableMap<LessonSubject, MutableList<TemplateLessonSlotDto>>> = mutableMapOf()

    fun addLesson(template: LessonTemplate, lesson: TemplateLessonSlot): LessonTemplateDto {
        var subjectMap: MutableMap<LessonSubject, MutableList<TemplateLessonSlotDto>>? = lessons[lesson.day]
        if (subjectMap == null) {
            subjectMap = mutableMapOf(Pair(lesson.subject, mutableListOf<TemplateLessonSlotDto>()))
            lessons[lesson.day] = subjectMap
        }

        var lessonSlots: MutableList<TemplateLessonSlotDto>? = subjectMap[lesson.subject]
        if (lessonSlots == null) {
            lessonSlots = mutableListOf<TemplateLessonSlotDto>()
            subjectMap[lesson.subject] = lessonSlots
        }

        val lessonSlotDto = TemplateLessonSlotDto().apply {
            id = lessonId(template, lesson)
            fromMins = lesson.fromMins
            ageGroup = lesson.ageGroup
        }
        lessonSlots.add(lessonSlotDto)
        return this
    }

    fun lessonId(template: LessonTemplate, lesson: TemplateLessonSlot): String =
            "t${template.id}-${lesson.day}-${lesson.subject}-${DateTimeUtils.minutesToTime(lesson.fromMins)}"

    companion object {
        fun fromDomainObject(template: LessonTemplate): LessonTemplateDto {
            val lessonTemplateDto = LessonTemplateDto().apply {
                id = template.id ?: 0
                name = template.name
                startDate = template.startDate
                endDate = template.endDate
            }
            template.lessons.forEach { lessonTemplateDto.addLesson(template, it) }
            return lessonTemplateDto
        }

    }
}

class TemplateLessonSlotDto() {
    var id: String = ""
    var fromMins: Int = 0
    var ageGroup: LessonAgeGroup = LessonAgeGroup.g2_3
}

class LessonSlotDto() {
    var id: String = ""
    var status: LessonSlotStatus = LessonSlotStatus.planned
    var teacherIds: Collection<Long> = emptySet()
    var visitsSummary: VisitsSummaryDto = VisitsSummaryDto()
    var studentSlots: Map<Long, StudentSlotDto> = mapOf()
}

open class StudentSlotBaseDto(
        val id: Long,
        val visitType: VisitType,
        val repeatsLeft: Int,
        val status: StudentSlotStatus,
        val cancelType: LessonCancelType?,
        val invalidated: Boolean
)

class StudentSlotDto(
        id: Long,
        visitType: VisitType,
        repeatsLeft: Int,
        status: StudentSlotStatus,
        cancelType: LessonCancelType?,
        invalidated: Boolean,
        val studentId: Long,
        val cardId: Long
) : StudentSlotBaseDto(id, visitType, repeatsLeft, status, cancelType, invalidated) {

    companion object {
        fun fromDomainObject(studentSlot: StudentSlot): StudentSlotDto {
            return StudentSlotDto(studentSlot.id!!,
                    studentSlot.visitType,
                    studentSlot.repeatsLeft,
                    studentSlot.status,
                    studentSlot.cancelType,
                    studentSlot.invalidated,
                    studentSlot.student!!.id!!,
                    studentSlot.card!!.id!!)
        }
    }
}

class VisitsSummaryDto {
    var total: Int = 0
    var regular: Int = 0
    var trial: Int = 0
    var bonus: Int = 0

    companion object {
        fun fromDomainObject(studentSlots: Collection<StudentSlot>): VisitsSummaryDto {
            val visits: VisitsSummaryDto = VisitsSummaryDto()
            studentSlots.forEach {
                if (it.status != StudentSlotStatus.canceled) {
                    when (it.visitType) {
                        VisitType.regular -> visits.regular++
                        VisitType.trial -> visits.trial++
                        VisitType.bonus -> visits.bonus++
                        VisitType.transfer -> visits.regular++
                    }
                }
            }
            visits.total = visits.regular + visits.trial + visits.bonus
            return visits
        }
    }
}

class LessonStudentDto {
    var id: Long = 0
    var businessId: String = ""
    var nameCn: String? = ""
    var nameEn: String? = ""
    var gender: Gender = Gender.boy
    var birthDate: LocalDate = Student.defaultStudentBirthDate
    var presentInSchool: Boolean = false

    companion object {
        fun fromDomainObject(student: Student, presentInSchool: Boolean): LessonStudentDto {
            return LessonStudentDto().apply {
                id = student.id!!
                businessId = student.businessId
                nameCn = student.nameCn
                nameEn = student.nameEn
                gender = student.gender
                birthDate = student.birthDate
                this.presentInSchool = presentInSchool
            }
        }
    }
}