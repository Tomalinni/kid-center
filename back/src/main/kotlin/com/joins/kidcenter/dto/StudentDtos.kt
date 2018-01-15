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

package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.EnumUtils
import java.time.LocalDate
import java.time.LocalDateTime

class StudentDashboardDto {
    var id: Long = 0
    var businessId: String = ""
    var trialBusinessId: String = ""
    var status: StudentStatus = StudentStatus.registered
    var nameEn: String? = ""
    var nameCn: String? = ""
    var birthDate: LocalDate = Student.defaultStudentBirthDate
    var gender: Gender = Gender.boy
    var kinderGarden: KinderGarden? = null
    var comment: String? = null
    val lessonsSummary: MutableMap<VisitType, StudentDashboardLessonTypeSummary> = mutableMapOf()
    val cards: MutableSet<StudentDashboardStudentCardDto> = mutableSetOf()
    var relatives: MutableList<StudentRelative> = mutableListOf()
    var promoter: StudentNamesDto? = null
    var promotedStudents: MutableList<StudentNamesDto> = mutableListOf()
    var promotionSource: PromotionSourceDto? = null
    var promotionDetail: PromotionDetailDto? = null
    var photoName: String? = null
    var presentInSchool: Boolean = false
    var siblings: MutableList<StudentNamesDto> = mutableListOf()
}

class StudentDashboardLessonTypeSummary {
    var total: Int = 0
    var used: Int = 0
    var available: Int = 0
    var expired: Int = 0
    var planned: Int = 0
    var visited: Int = 0
    var missed: Int = 0
    var canceled: Int = 0
    var suspended: Int = 0
}

class StudentDashboardLesson(
        id: Long,
        visitType: VisitType,
        repeatsLeft: Int,
        status: StudentSlotStatus,
        cancelType: LessonCancelType?,
        invalidated: Boolean,
        val lessonId: String,
        val ageGroup: LessonAgeGroup,
        val modifiedDate: LocalDateTime,
        val modifiedBy: String
) : StudentSlotBaseDto(id, visitType, repeatsLeft, status, cancelType, invalidated) {

    fun equivalent(lesson: StudentDashboardLesson): Boolean {
        return visitType == lesson.visitType &&
                repeatsLeft == lesson.repeatsLeft &&
                status == lesson.status &&
                cancelType == lesson.cancelType &&
                invalidated == lesson.invalidated &&
                lessonId == lesson.lessonId &&
                ageGroup == lesson.ageGroup
    }
}

class StudentNamesDto {
    var id: Long = 0
    var nameEn: String? = ""
    var nameCn: String? = ""
    var businessId: String = ""

    companion object {
        fun fromDomainObject(student: Student): StudentNamesDto {
            return StudentNamesDto().apply {
                id = student.id!!
                businessId = student.businessId
                nameEn = student.nameEn
                nameCn = student.nameCn
            }
        }
    }
}

class StudentRelativesDto {
    var studentId: Long = 0
    var relatives: MutableList<StudentRelativeNotificationsDto> = mutableListOf()

    fun relativeById(id: Long?): StudentRelativeNotificationsDto? {
        if (id == null) {
            return null
        }
        relatives.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }
}

class StudentRelativeNotificationsDto {
    var id: Long = 0
    var mobileNotifications: MutableSet<MobileNotification> = MobileNotification.values().toMutableSet()
    var emailNotifications: MutableSet<EmailNotification> = EmailNotification.values().toMutableSet()
}

class PromotionSourceDto {
    var id: Long? = null
    var name: String = ""
    var hasPromoter: Boolean = false

    companion object {
        fun fromDomainObject(promotionSource: PromotionSource): PromotionSourceDto {
            return PromotionSourceDto().apply {
                id = promotionSource.id!!
                name = promotionSource.name
                hasPromoter = promotionSource.hasPromoter
            }
        }
    }
}

class PromotionDetailDto {
    var id: Long? = null
    var name: String = ""
    var promotionSource: EntityRef<Long>? = null

    companion object {
        fun fromDomainObject(promotionDetail: PromotionDetail): PromotionDetailDto {
            return PromotionDetailDto().apply {
                id = promotionDetail.id!!
                name = promotionDetail.name
                promotionSource = EntityRef(promotionDetail.promotionSource!!.id!!)
            }
        }
    }
}

class StudentDashboardStudentCardDto {
    var id: Long = 0
    var cardId: Long? = null
    var ageRange: AgeRange = AgeRange.r2_7
    var allowedSubjectsMask: Int = EnumUtils.defaultBitmask(LessonSubject.values()) //all lessons allowed
    var price: Int = 0
    var visitType: VisitType = VisitType.regular
    var activationDate: LocalDate = DateTimeUtils.currentDate()
    var purchaseDate: LocalDate = DateTimeUtils.currentDate()
    var durationDays: Int = 0
    var lessonsLimit: Int = 0
    var lessonsAvailable: Int = 0
    var cancelsLimit: Int = 0
    var cancelsAvailable: Int = 0
    var lateCancelsLimit: Int = 0
    var lateCancelsAvailable: Int = 0
    var lastMomentCancelsLimit: Int = 0
    var lastMomentCancelsAvailable: Int = 0
    var undueCancelsLimit: Int = 0
    var undueCancelsAvailable: Int = 0
    var missLimit: Int = 0
    var missAvailable: Int = 0
    var suspendsLimit: Int = 0
    var suspendsAvailable: Int = 0
    var payment: EntityRef<Long>? = null

    companion object {
        fun fromDomainObject(card: StudentCard): StudentDashboardStudentCardDto {
            return StudentDashboardStudentCardDto().apply {
                id = card.id!!
                cardId = card.cardId
                ageRange = card.ageRange
                allowedSubjectsMask = card.allowedSubjectsMask
                price = card.price
                visitType = card.visitType
                activationDate = card.activationDate
                purchaseDate = card.purchaseDate
                durationDays = card.durationDays
                lessonsLimit = card.lessonsLimit
                lessonsAvailable = card.lessonsAvailable
                cancelsLimit = card.cancelsLimit
                cancelsAvailable = card.cancelsAvailable
                lateCancelsLimit = card.lateCancelsLimit
                lateCancelsAvailable = card.lateCancelsAvailable
                lastMomentCancelsLimit = card.lastMomentCancelsLimit
                lastMomentCancelsAvailable = card.lastMomentCancelsAvailable
                undueCancelsLimit = card.undueCancelsLimit
                undueCancelsAvailable = card.undueCancelsAvailable
                missLimit = card.missLimit
                missAvailable = card.missAvailable
                suspendsLimit = card.suspendsLimit
                suspendsAvailable = card.suspendsAvailable
                payment = if (card.payment != null) EntityRef(card.payment!!.id!!) else null
            }
        }
    }
}

class StudentIdCardId(val studentId: Long,
                      val cardId: Long)