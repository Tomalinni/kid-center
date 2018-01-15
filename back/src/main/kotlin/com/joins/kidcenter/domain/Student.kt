package com.joins.kidcenter.domain

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.EnumUtils
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Size
import kotlin.reflect.KMutableProperty0

@Entity
@JsonFilter(StudentMetadata.Filter.id)
class Student : AuditableEntity() {
    var nameEn: String? = ""
    var nameCn: String? = ""
    var birthDate: LocalDate = defaultStudentBirthDate
    var gender: Gender = Gender.boy

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "manager_id", foreignKey = ForeignKey(name = "fk_student_manager"))
    var manager: Teacher? = null
    @ManyToOne
    var kinderGarden: KinderGarden? = null
    @ManyToOne
    @JoinColumn(name = "promotion_source", foreignKey = ForeignKey(name = "stud_promotion_source_fkey"))
    var promotionSource: PromotionSource? = null
    @ManyToOne
    @JoinColumn(name = "promotion_detail", foreignKey = ForeignKey(name = "stud_promotion_detail_fkey"))
    var promotionDetail: PromotionDetail? = null
    @ManyToOne
    @JoinColumn(name = "promoter")
    @JsonFilter(StudentMetadata.Filter.referenceAndName)
    var promoter: Student? = null

    var status: StudentStatus = StudentStatus.registered
    var businessId: String = ""
    var trialBusinessId: String = ""
    var primaryPhotoName: String? = null
    @Size(max = 512)
    var comment: String? = null

    /**
     * Is mobile of any relative confirmed. Optimization field
     */
    @Column(name = "mobile_confirmed")
    var mobileConfirmed: Boolean = false
    var impression: StudentImpression = StudentImpression.notDefined

    @ManyToMany(cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH))
    @JoinTable(name = "student_studentrelative",
            joinColumns = arrayOf(JoinColumn(name = "student_id", referencedColumnName = "id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "relatives_id", referencedColumnName = "id")),
            foreignKey = ForeignKey(name = "stud_rel_stud_id_fkey"),
            inverseForeignKey = ForeignKey(name = "stud_rel_rel_id_id_fkey"))
    var relatives: MutableList<StudentRelative> = ArrayList()
    @ManyToOne
    @JoinColumn(name = "family_id")
    var family: StudentFamily? = null

    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "student")
    var cards: MutableSet<StudentCard> = mutableSetOf()

    //calculated field
    @Column(name = "planned_lessons_count")
    var plannedLessonsCount: Int = 0
    //calculated field
    @Column(name = "visited_lessons_count")
    var visitedLessonsCount: Int = 0
    //calculated field
    @Column(name = "paid_lessons_count")
    var paidLessonsCount: Int = 0
    //calculated field
    @Column(name = "bonus_lessons_count")
    var bonusLessonsCount: Int = 0
    //calculated field
    @Column(name = "used_lessons_count")
    var usedLessonsCount: Int = 0
    //calculated field
    @Column(name = "avl_lessons_count")
    var availableLessonsCount: Int = 0
    //calculated field
    //Last day when any card is still valid
    @Column(name = "last_card_valid_date")
    var lastCardValidDate: LocalDate? = null
    /**
     * First registered relative name
     */
    @Column(name = "primary_relative")
    var primaryRelativeName: String = ""
    /**
     * Students count advised by this student
     */
    @Column(name = "promoted_student_count")
    var promotedStudentCount: Int = 0

    /**
     * Collection of photo names
     */
    @Transient
    var photos: Collection<String> = emptyList()
    /**
     * Collection next student lesson ids
     */
    @Transient
    var nextLessons: List<String> = emptyList()
    /**
     * Collection of sisters and brothers
     */
    @Transient
    @JsonFilter(StudentMetadata.Filter.info)
    var siblings: List<Student> = emptyList()

    companion object {
        val defaultStudentBirthDate = DateTimeUtils.currentDate().minusYears(4)
    }
}

class StudentMetadata {
    companion object Filter {
        const val id = "studentFilter"
        const val referenceAndName = "studentPromoterFilter"
        const val info = "infoFilter"
        val listItemIncludedFields = arrayOf("id", "createdDate", "nameEn", "nameCn", "birthDate", "gender", "manager", "isTrial", "businessId", "status", "promotionSource", "promotionDetail", "mobileConfirmed", "plannedLessonsCount", "visitedLessonsCount", "paidLessonsCount", "bonusLessonsCount", "usedLessonsCount", "availableLessonsCount", "lastCardValidDate", "nextLessons", "primaryRelativeName", "promotedStudentCount")
        val listItemWithCardsIncludedFields = listItemIncludedFields + arrayOf("cards")
        val formExcludedFields = arrayOf("cards", "impression")
        val referenceIncludedFields = arrayOf("id")
        val referenceAndNameIncludedFields = arrayOf("id", "nameCn")
        val infoIncludedFields = arrayOf("id", "nameCn", "nameEn", "trialBusinessId", "businessId", "gender", "birthDate")
    }
}

@Entity
class StudentRelative : BasicEntity(), Cloneable {
    var role: String = ""
    var name: String? = ""
    var mail: String? = ""
    var driverLicense: String? = ""
    var passport: String? = ""
    var mobile: String? = ""
    var mobileConfirmed: Boolean = false
    var primaryPhotoName: String? = null
    @ManyToMany(mappedBy = "relatives")
    @JsonIgnore
    var students: MutableSet<Student> = mutableSetOf()
    @ManyToOne
    @JoinColumn(name = "family_id")
    var family: StudentFamily? = null
    @ElementCollection(targetClass = MobileNotification::class)
    @Enumerated(EnumType.STRING)
    var mobileNotifications: MutableSet<MobileNotification> = MobileNotification.values().toMutableSet()
    @ElementCollection(targetClass = EmailNotification::class)
    @Enumerated(EnumType.STRING)
    var emailNotifications: MutableSet<EmailNotification> = EmailNotification.values().toMutableSet()

    /**
     * Collection of photo names
     */
    @Transient
    var photos: Collection<String> = emptyList()
    /**
     * Confirmation code used to verify mobile number
     */
    @Transient
    var confirmationCode: String = ""
    /**
     * Confirmation id associated with verify process
     */
    @Transient
    var confirmationId: String? = null

    override public fun clone(): StudentRelative {
        return super.clone() as StudentRelative
    }
}

@Entity
@Table(name = "student_family")
class StudentFamily : Persistable {
    @Id
    @SequenceGenerator(name = "student_family_gen", sequenceName = "student_family_seq", allocationSize = 1)
    @GeneratedValue(generator = "student_family_gen")
    override var id: Long? = null
}


class StudentCardMetadata {
    companion object Filter {
        const val id = "studentCardFilter"
        val studentCol = arrayOf("student")
    }
}

@Entity
@JsonFilter(StudentCardMetadata.Filter.id)
class StudentCard : Persistable {
    @Id
    @SequenceGenerator(name = "student_card_gen", sequenceName = "student_card_seq", allocationSize = 1)
    @GeneratedValue(generator = "student_card_gen")
    override var id: Long? = null
    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "student_id")
    var student: Student? = null

    //reference to original card. Do not use in calculations of available values
    @Column(name = "card_id")
    var cardId: Long? = null

    @Enumerated(EnumType.STRING)
    var ageRange: AgeRange = AgeRange.r2_7
    var price: Int = 0
    var visitType: VisitType = VisitType.regular
    var activationDate: LocalDate = DateTimeUtils.currentDate()
    var purchaseDate: LocalDate = DateTimeUtils.currentDate()
    var durationDays: Int = 0
    var lessonsLimit: Int = 0
    var lessonsAvailable: Int = 0
    var cancelsLimit: Int = 0
    var cancelsAvailable: Int = 0
    @Column(name = "late_cancels_limit")
    var lateCancelsLimit: Int = 0
    @Column(name = "late_cancels_available")
    var lateCancelsAvailable: Int = 0
    @Column(name = "last_moment_cancels_limit")
    var lastMomentCancelsLimit: Int = 0
    @Column(name = "last_moment_cancels_available")
    var lastMomentCancelsAvailable: Int = 0
    @Column(name = "undue_cancels_limit")
    var undueCancelsLimit: Int = 0
    @Column(name = "undue_cancels_available")
    var undueCancelsAvailable: Int = 0
    @Column(name = "miss_limit")
    var missLimit: Int = 0
    @Column(name = "miss_available")
    var missAvailable: Int = 0
    var suspendsLimit: Int = 0
    var suspendsAvailable: Int = 0
    var allowedSubjectsMask: Int = EnumUtils.defaultBitmask(LessonSubject.values()) //all lessons allowed
    @OneToOne(cascade = arrayOf())
    @JoinColumn(name = "payment_id", foreignKey = ForeignKey(name = "fk_student_card_payment"))
    var payment: Payment? = null

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "source_transfer_card_id", foreignKey = ForeignKey(name = "fk_student_card_transfer_card"))
    var sourceTransferCard: StudentCard? = null

    //calculated field
    @Column(name = "planned_lessons_count")
    var plannedLessonsCount: Int = 0
    //calculated field
    @Column(name = "spent_lessons_count")
    var spentLessonsCount: Int = 0

    fun definedEndDate(): LocalDate =
            activationDate.plusDays(durationDays.toLong())

    fun cancelsAvailableProperty(cancelType: LessonCancelType): KMutableProperty0<Int> {
        return when (cancelType) {
            LessonCancelType.normal -> this::cancelsAvailable
            LessonCancelType.late -> this::lateCancelsAvailable
            LessonCancelType.lastMoment -> this::lastMomentCancelsAvailable
            LessonCancelType.undue -> this::undueCancelsAvailable
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as StudentCard

        if (id == null || id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }


}

@Entity
class StudentRelativeRole() {
    @Id
    @SequenceGenerator(name = "student_relative_role_gen", sequenceName = "student_relative_role_seq", allocationSize = 1)
    @GeneratedValue(generator = "student_relative_role_gen")
    var id: Long? = null
    var name: String = ""
}

@Entity
class KinderGarden() {
    @Id
    @SequenceGenerator(name = "kinder_garden_gen", sequenceName = "kinder_garden_seq", allocationSize = 1)
    @GeneratedValue(generator = "kinder_garden_gen")
    var id: Long? = null
    var name: String = ""
    var address: String = ""
    var phone: String = ""
}

@Entity
class PromotionSource() {
    @Id
    @SequenceGenerator(name = "promotion_source_gen", sequenceName = "promotion_source_seq", allocationSize = 1)
    @GeneratedValue(generator = "promotion_source_gen")
    var id: Long? = null
    var name: String = ""
    var hasPromoter: Boolean = false
}

@Entity
class PromotionDetail() {
    @Id
    @SequenceGenerator(name = "promotion_detail_gen", sequenceName = "promotion_detail_seq", allocationSize = 1)
    @GeneratedValue(generator = "promotion_detail_gen")
    var id: Long? = null
    var name: String = ""
    @ManyToOne
    @JoinColumn(name = "promotion_source_id",
            foreignKey = ForeignKey(name = "fk_promotiondetail_source"))
    @JsonIgnore
    var promotionSource: PromotionSource? = null
}


enum class Gender {
    boy, girl
}

enum class StudentStatus {
    registered, lessonPlanned, lessonVisited, trialEnd, cardPaid
}
