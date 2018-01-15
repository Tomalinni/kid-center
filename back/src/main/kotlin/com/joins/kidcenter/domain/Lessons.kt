package com.joins.kidcenter.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.joins.kidcenter.dto.DateRange
import com.joins.kidcenter.dto.lessons.StudentSlotId
import com.joins.kidcenter.utils.DateTimeUtils
import java.text.DateFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class LessonSlot {
    @Id
    var id: String = ""
    var dateTime: LocalDateTime = LocalDateTime.MIN
    var subject: LessonSubject = LessonSubject.fitness
    var ageGroup: LessonAgeGroup = LessonAgeGroup.g2_3
    var status: LessonSlotStatus = LessonSlotStatus.planned

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, mappedBy = "lesson")
    var students: MutableSet<StudentSlot> = HashSet()
}

@Entity
class StudentSlot : AuditableEntity() {
    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "lessonslot_id")
    var lesson: LessonSlot? = null
    @ManyToOne(cascade = arrayOf())
    var student: Student? = null

    @ManyToOne(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH))
    var card: StudentCard? = null
    var visitType: VisitType = VisitType.regular
    var repeatsLeft: Int = 1
    var status: StudentSlotStatus = StudentSlotStatus.planned
        private set
    @Column(name = "cancel_type")
    var cancelType: LessonCancelType? = null
        private set
    /**
     * True if slot cancel was invoked without available cancels. Also after such cancel, status will be set to missed.
     * Invalidated lesson is counted as used.
     */
    var invalidated: Boolean = false

    fun setStatus(status: StudentSlotStatus, cancelType: LessonCancelType? = null) {
        this.status = status
        this.cancelType = if (status != StudentSlotStatus.canceled) null else cancelType
    }

    @JsonIgnore
    fun businessId(): String = StudentSlotId(lesson!!, student!!).id

    @JsonIgnore
    fun isRepeated(): Boolean = repeatsLeft > 1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StudentSlot) return false
        return lesson?.id == other.lesson?.id
                && student?.id == other.student?.id
                && card?.id == other.card?.id
                && status == other.status
    }

    override fun hashCode(): Int {
        var result = lesson?.hashCode() ?: 0
        result = 31 * result + (student?.hashCode() ?: 0)
        result = 31 * result + (card?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        return result
    }
}

@Entity
class LessonTemplate : Persistable {
    @Id
    @SequenceGenerator(name = "lesson_template_gen", sequenceName = "lesson_template_seq", allocationSize = 1)
    @GeneratedValue(generator = "lesson_template_gen")
    override var id: Long? = null
    var name: String = ""
    var startDate: LocalDate = LessonTemplate.minStartDate
    var endDate: LocalDate = LessonTemplate.maxEndDate

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    @JoinColumn(name = "lesson_template_id")
    var lessons: MutableList<TemplateLessonSlot> = ArrayList()

    companion object {
        fun normalize(template: LessonTemplate) {
            if (template.startDate.isAfter(template.endDate)) {
                val startDate = template.startDate
                template.startDate = template.endDate
                template.endDate = startDate
            }
        }

        fun nameFromDateRange(dates: DateRange): String {
            if (dates.startDate == minStartDate && dates.endDate == maxEndDate) {
                return "Any time"
            } else if (dates.startDate != minStartDate && dates.endDate == maxEndDate) {
                return "From ${dates.startDate}"
            } else if (dates.endDate != maxEndDate && dates.startDate == minStartDate) {
                return "Before ${dates.endDate}"
            } else {
                return "From ${dates.startDate} to ${dates.endDate}"
            }
        }

        fun copyFrom(template: LessonTemplate, dates: DateRange): LessonTemplate {
            val newTemplate = LessonTemplate()
            newTemplate.startDate = dates.startDate
            newTemplate.endDate = dates.endDate
            newTemplate.name = nameFromDateRange(dates)
            newTemplate.lessons = template.lessons.map { TemplateLessonSlot(it) }.toMutableList()
            return newTemplate
        }

        val minStartDate: LocalDate = LocalDate.of(2000, 1, 1)
        val maxEndDate: LocalDate = LocalDate.of(2100, 1, 1)
    }
}

@Entity
class TemplateLessonSlot() : Persistable {
    constructor(template: TemplateLessonSlot) : this() {
        day = template.day
        fromMins = template.fromMins
        subject = template.subject
        ageGroup = template.ageGroup
    }

    @Id
    @SequenceGenerator(name = "template_lesson_slot_gen", sequenceName = "template_lesson_slot_seq", allocationSize = 1)
    @GeneratedValue(generator = "template_lesson_slot_gen")
    override var id: Long? = null
    var day: LessonDay = LessonDay.monday
    var fromMins: Int = 0
    var subject: LessonSubject = LessonSubject.fitness
    var ageGroup: LessonAgeGroup = LessonAgeGroup.g2_3
}

@Entity
class Homework : Persistable {
    @Id
    @SequenceGenerator(name = "homework_gen", sequenceName = "homework_seq", allocationSize = 1)
    @GeneratedValue(generator = "homework_gen")
    override var id: Long? = null
    var subject: LessonSubject? = LessonSubject.art
    var ageGroup: LessonAgeGroup = LessonAgeGroup.g2_3
    var startDate: LocalDate = DateTimeUtils.currentDate()
    var endDate: LocalDate = DateTimeUtils.currentDate()
}

@Entity
@Table(name = "school_visit")
class SchoolVisit : Persistable {
    @Id
    @SequenceGenerator(name = "school_visit_gen", sequenceName = "school_visit_seq", allocationSize = 1)
    @GeneratedValue(generator = "school_visit_gen")
    override var id: Long? = null

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "student_id", foreignKey = ForeignKey(name = "fk_school_visit_student"))
    var student: Student? = null
    var date: LocalDate = DateTimeUtils.currentDate()
}

enum class LessonSlotStatus {
    planned, closed, revoked, removed
}

enum class StudentSlotStatus {
    planned, visited, missed, revoked, canceled, removed
}

enum class LessonSubject(val prefix: String, val labelEn: String, val labelCn: String, val duration: Int) {
    cooking("c", "Cooking", "烹饪", 60),
    art("a", "Art", "美术", 60),
    english("e", "Craft", "手工", 60),
    lego("l", "Builder", "积木", 60),
    fitness("f", "Fitness", "健身", 60),
    ballet("b", "Ballet", "芭蕾", 60),
    z_sFitness("z", "Fitness", "体育", 30),
    y_sMusic("y", "Music", "音乐", 30),
    x_sArt("x", "Art", "美术", 30);

    fun translated(): String {
        return "$labelCn-$labelEn"
    }

    companion object {
        fun fromPrefix(prefix: String): LessonSubject? {
            return LessonSubject.values().find { it.prefix == prefix }
        }
    }
}

enum class LessonDay {
    monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    /**
     * Names array starting from sunday
     */
    private var weekdays = DateFormatSymbols(Locale.CHINESE).shortWeekdays

    fun toWeekDay(): DayOfWeek {
        return DayOfWeek.values()[this.ordinal]
    }

    fun dayName(): String {
        return weekdays[(this.ordinal + 1) % 7 + 1]
    }

    companion object {
        fun fromWeekDay(day: DayOfWeek): LessonDay {
            return LessonDay.values()[day.ordinal]
        }
    }
}

enum class LessonAgeGroup {
    g2_3, g3_5, g5_7, g1_1y5, g1y5_2
}

/**
 * @param minutesBeforeLessonStart time before lesson start in minutes by which corresponding type is selected at lesson cancel
 * @param studentPresentInSchool whether student presents at school at time of cancel, null value for any possible value
 */
enum class LessonCancelType {
    normal,
    late,
    lastMoment,
    undue; //can cancel before lesson ends

    companion object {
        fun forLesson(lessonSlot: LessonSlot, studentPresentInSchool: Boolean): LessonCancelType? {
            val currentDateTime = DateTimeUtils.currentDateTime()
            val lessonDateTime = lessonSlot.dateTime
            if (currentDateTime.plusMinutes(DateTimeUtils.minutesInDay.toLong()).isBefore(lessonDateTime)) {
                return normal
            } else if (currentDateTime.isBefore(lessonDateTime)) {
                return if (studentPresentInSchool) lastMoment else late
            } else if (currentDateTime.minusMinutes(lessonSlot.subject.duration.toLong()).isBefore(lessonDateTime)) {
                return undue
            }
            return null
        }
    }
}
