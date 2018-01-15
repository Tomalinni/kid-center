package com.joins.kidcenter.utils

import com.joins.kidcenter.Config
import com.joins.kidcenter.controller.AuthResponse
import com.joins.kidcenter.controller.RegisterRequest
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.dto.internal.ParametrizedMessage
import com.joins.kidcenter.dto.lessons.LessonTemplateInitDto
import java.time.LocalDate
import java.time.Period

object ValidatorsUtil {
    val NAME_MAX_LENGTH = 50
    val ID_MAX_LENGTH = 50
    val MOBILE_LENGTH = 11
    val DRIVER_LICENSE_LENGTH = 25
    val PASSPORT_LENGTH = 20
    val mobileRegex = Regex("^\\d+$")
    val literalNumericRegex = Regex("^[a-zA-Z0-9]+$")
    val stringIdRegex = Regex("^[a-zA-Z0-9_]+$")

    fun validateTeacher(entity: Teacher): SaveResult<Teacher> {
        val sr = SaveResult<Teacher>()
        sr.addValidationMessage(EntityId.teachers, "name", validateName(entity.name))
        return sr
    }

    fun validatePayment(entity: Payment): SaveResult<Payment> {
        val sr = SaveResult<Payment>()
        if (entity.school == null && PaymentDirection.requiredAccountDirections.contains(entity.direction)) {
            sr.addValidationMessage(EntityId.payments, "school", "common.validation.field.not.empty")
        }
        if (entity.account == null && PaymentDirection.requiredAccountDirections.contains(entity.direction)) {
            sr.addValidationMessage(EntityId.payments, "account", "common.validation.field.not.empty")
        }
        if (entity.targetSchool == null && PaymentDirection.requiredTargetAccountDirections.contains(entity.direction)) {
            sr.addValidationMessage(EntityId.payments, "targetSchool", "common.validation.field.not.empty")
        }
        if (entity.targetAccount == null && PaymentDirection.requiredTargetAccountDirections.contains(entity.direction)) {
            sr.addValidationMessage(EntityId.payments, "targetAccount", "common.validation.field.not.empty")
        }

        if (entity.category == null) {
            sr.addValidationMessage(EntityId.payments, "category", "common.validation.field.not.empty")
        }

        sr.addValidationMessage(EntityId.payments, "price", validateNotNegativeNumber(entity.price.toInt()))
        return sr
    }

    fun validateCard(entity: Card): SaveResult<Card> {
        val sr = SaveResult<Card>()

        sr.addValidationMessage(EntityId.cards, "price", validateNotNegativeNumber(entity.price))
        if (entity.visitType.chargeless && entity.price > 0) {
            sr.addValidationMessage(EntityId.cards, "price", "common.validation.card.positive.price.for.chargeless.card")
        }

        sr.addValidationMessage(EntityId.cards, "maxDiscount", validateNotNegativeNumber(entity.maxDiscount))
        if (entity.maxDiscount > entity.price) {
            sr.addValidationMessage(EntityId.cards, "maxDiscount", "common.validation.card.discount.exceeds.price")
        }

        sr.addValidationMessage(EntityId.cards, "durationDays", validateNotNegativeNumber(entity.durationDays))
        if (entity.durationDays > entity.durationDaysMax) {
            sr.addValidationMessage(EntityId.cards, "durationDays", "common.validation.card.min.duration.exceeds.max.duration")
        }

        if (entity.allowedSubjectsMask <= 0) {
            sr.addValidationMessage(EntityId.cards, "allowedSubjects", "common.validation.field.not.empty")
        }

        sr.addValidationMessage(EntityId.cards, "lessonsLimit", validateNotNegativeNumber(entity.lessonsLimit))
        sr.addValidationMessage(EntityId.cards, "cancelsLimit", validateNotNegativeNumber(entity.cancelsLimit))
        sr.addValidationMessage(EntityId.cards, "lateCancelsLimit", validateNotNegativeNumber(entity.lateCancelsLimit))
        sr.addValidationMessage(EntityId.cards, "lastMomentCancelsLimit", validateNotNegativeNumber(entity.lastMomentCancelsLimit))
        sr.addValidationMessage(EntityId.cards, "undueCancelsLimit", validateNotNegativeNumber(entity.undueCancelsLimit))
        sr.addValidationMessage(EntityId.cards, "missLimit", validateNotNegativeNumber(entity.missLimit))
        sr.addValidationMessage(EntityId.cards, "suspendsLimit", validateNotNegativeNumber(entity.suspendsLimit))

        return sr
    }

    fun validateStudentCall(entity: StudentCall): SaveResult<StudentCall> {
        val sr = SaveResult<StudentCall>()

        sr.addValidationMessage(EntityId.studentCalls, "name", validateDefined(entity.employee))

        return sr
    }

    fun validateStudent(entity: Student): SaveResult<Student> {
        val sr = SaveResult<Student>()
        if (entity.nameEn.isNullOrBlank() && entity.nameCn.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.students, Config.commonValidationMessage, "common.students.validation.names.not.empty")
        }

        if (entity.nameEn != null && entity.nameEn!!.length > NAME_MAX_LENGTH) {
            sr.addValidationMessage(EntityId.students, "nameEn", ParametrizedMessage("common.validation.field.max.length", NAME_MAX_LENGTH))
        }
        if (entity.nameCn != null && entity.nameCn!!.length > NAME_MAX_LENGTH) {
            sr.addValidationMessage(EntityId.students, "nameCn", ParametrizedMessage("common.validation.field.max.length", NAME_MAX_LENGTH))
        }

        val age = getAge(entity.birthDate)
        if (age < Config.minStudentAgeYears) {
            sr.addValidationMessage(EntityId.students, "birthDate", ParametrizedMessage("common.students.validation.young.age", Config.minStudentAgeYears))
        }
        if (age > Config.maxStudentAgeYears) {
            sr.addValidationMessage(EntityId.students, "birthDate", ParametrizedMessage("common.validation.field.not.empty", Config.maxStudentAgeYears))
        }

        for ((index, relative) in entity.relatives.withIndex()) {
            validateRelative(relative, index, sr)
        }

        return sr
    }

    fun validateStudentCard(entity: StudentCard, entityId: EntityId): SaveResult<StudentCard> {
        val sr = SaveResult<StudentCard>()

        if (entity.price < 0) {
            sr.addValidationMessage(entityId, "price", "common.validation.not.negative")
        }
        if (entity.durationDays < 0) {
            sr.addValidationMessage(entityId, "durationDays", "common.validation.not.negative")
        }

        return sr
    }

    fun validateStudentCardCreateRequest(entity: StudentCardPaymentRequest): SaveResult<StudentCard> {
        val sr = SaveResult<StudentCard>()

        val card = entity.card
        if (card == null) {
            sr.addValidationMessage(EntityId.studentCardPayment, Config.commonValidationMessage, "common.error.student.card.card.not.defined")
        }
        if (card!!.price > 0) {
            if (entity.accountType == null) {
                sr.addValidationMessage(EntityId.studentCardPayment, "accountType", "common.validation.field.not.empty")
            }
            if (entity.targetAccount?.id == null) {
                sr.addValidationMessage(EntityId.studentCardPayment, "targetAccount", "common.validation.field.not.empty")
            }
            if (entity.targetPartner?.id == null) {
                sr.addValidationMessage(EntityId.studentCardPayment, "targetPartner", "common.validation.field.not.empty")
            }
        }

        return sr
    }

    fun validateRelative(entity: StudentRelative, index: Int, sr: SaveResult<Student>): SaveResult<Student> {
        if (entity.role.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.relatives, index, "role", ParametrizedMessage("common.validation.field.not.empty"))
        }

        sr.addValidationMessage(EntityId.relatives, index, "name", validateName(entity.name))
        sr.addValidationMessage(EntityId.relatives, index, "mail", validateEmail(entity.mail))

        if (!entity.driverLicense.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.relatives, index, "driverLicense", validateByChain(entity.driverLicense, arrayListOf(
                    { license -> select(license != null && !literalNumericRegex.matches(license), "common.relatives.validation.driverLicense.not.valid") },
                    { license -> select(license != null && license.length > DRIVER_LICENSE_LENGTH, ParametrizedMessage("common.validation.field.max.length", DRIVER_LICENSE_LENGTH)) }
            )))
        }

        if (!entity.passport.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.relatives, index, "passport", validateByChain(entity.passport, arrayListOf(
                    { passport -> select(passport != null && !literalNumericRegex.matches(passport), "common.relatives.validation.passport.not.valid") },
                    { passport -> select(passport != null && passport.length > PASSPORT_LENGTH, ParametrizedMessage("common.validation.field.max.length", PASSPORT_LENGTH)) }
            )))
        }

        if (!entity.mobile.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.relatives, index, "mobile", validateByChain(entity.mobile, arrayListOf(
                    { mobile -> select(!mobile.isNullOrEmpty() && !mobile!!.matches(mobileRegex), "common.validation.field.only.digits") },
                    { mobile -> select(!mobile.isNullOrEmpty() && mobile!!.length != MOBILE_LENGTH, ParametrizedMessage("common.validation.mobile.wrong.format", MOBILE_LENGTH)) }
            )))
        }

        return sr
    }

    fun validateProfile(entity: ProfileDto, actualUser: AppUser?): SaveResult<ProfileDto> {
        val sr = SaveResult<ProfileDto>()

        sr.addValidationMessage(EntityId.profile, "oldPass", select(entity.oldPass.isNullOrBlank(), "common.validation.field.not.blank"))
        sr.addValidationMessage(EntityId.profile, "newPass", select(entity.newPass.isNullOrBlank(), "common.validation.field.not.blank"))

        if (actualUser == null) {
            sr.addValidationMessage(EntityId.profile, Config.commonValidationMessage, "common.validation.profile.user.not.found")
        } else if (entity.oldPass != actualUser.pass) {
            sr.addValidationMessage(EntityId.profile, Config.commonValidationMessage, "common.validation.profile.wrong.old.password")
        }
        return sr
    }

    fun validateRegRelative(entity: RegisterRequest): SaveResult<AuthResponse> {
        val sr = SaveResult<AuthResponse>()
        if (entity.login.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.regRelatives, "login", "common.validation.field.not.blank")
        }

        if (!entity.mobile.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.regRelatives, "mobile", validateByChain(entity.mobile, arrayListOf(
                    { mobile -> select(!mobile.isNullOrEmpty() && !mobile.matches(mobileRegex), "common.validation.field.only.digits") },
                    { mobile -> select(!mobile.isNullOrEmpty() && mobile.length != MOBILE_LENGTH, ParametrizedMessage("common.validation.mobile.wrong.format", MOBILE_LENGTH)) }
            )))
        }

        if (entity.pass.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.regRelatives, "password", "common.validation.field.not.blank")
        }

        if (entity.pass != entity.passRepeat) {
            sr.addValidationMessage(EntityId.regRelatives, Config.commonValidationMessage, "common.validation.profile.wrong.password.confirmation")
        }

        return sr
    }

    @Suppress("UNUSED_PARAMETER")
    fun validateUser(entity: UserDto): SaveResult<UserDto> {
        val sr = SaveResult<UserDto>()
        if (entity.id.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.users, "newId", validateByChain(entity.newId, arrayListOf(
                    { newId -> select(entity.newId.isNullOrBlank(), "common.validation.field.not.blank") },
                    { newId -> select(entity.newId!!.length > ID_MAX_LENGTH, "common.validation.field.le.val") },
                    { newId -> select(!stringIdRegex.matches(entity.newId!!), "common.users.validation.user.id.not.valid") },
                    { newId -> select(entity.newId.equals("new"), "common.users.validation.user.already.exists") }
            )))
        } else {
            sr.addValidationMessage(EntityId.users, "id", validateByChain(entity.id, arrayListOf(
                    { id -> select(entity.id.length > ID_MAX_LENGTH, "common.validation.field.le.val") },
                    { id -> select(!stringIdRegex.matches(entity.id), "common.users.validation.user.id.not.valid") }
            )))
        }
        return sr
    }

    @Suppress("UNUSED_PARAMETER")
    fun validateRole(entity: RoleDto): SaveResult<RoleDto> {
        val sr = SaveResult<RoleDto>()
        if (entity.id.isNullOrBlank()) {
            sr.addValidationMessage(EntityId.roles, "newId", validateByChain(entity.newId, arrayListOf(
                    { newId -> select(entity.newId.isNullOrBlank(), "common.validation.field.not.blank") },
                    { newId -> select(entity.newId!!.length > ID_MAX_LENGTH, "common.validation.field.le.val") },
                    { newId -> select(!stringIdRegex.matches(entity.newId!!), "common.users.validation.user.id.not.valid") },
                    { newId -> select(entity.newId.equals("new"), "common.users.validation.user.already.exists") }
            )))
        } else {
            sr.addValidationMessage(EntityId.roles, "id", validateByChain(entity.id, arrayListOf(
                    { id -> select(entity.id.length > ID_MAX_LENGTH, "common.validation.field.le.val") },
                    { id -> select(!stringIdRegex.matches(entity.id), "common.users.validation.user.id.not.valid") }
            )))
        }
        return sr
    }

    fun validateHomework(entity: HomeworkDto): SaveResult<HomeworkDto> {
        val sr = SaveResult<HomeworkDto>()

        sr.addValidationMessage(EntityId.homework, Config.commonValidationMessage, select(!entity.startDate.isBefore(entity.endDate), "common.homework.validation.dates.format"))

        return sr
    }

    fun validateLessonTemplate(entity: LessonTemplateInitDto): SaveResult<LessonTemplate> {
        val sr = SaveResult<LessonTemplate>()
        if (entity.endDate.isBefore(DateTimeUtils.currentDate())) {
            sr.addValidationMessage(EntityId.lessonTemplates, "endDate", "common.error.template.end.date.before.now")
        }
        if (entity.endDate.isBefore(entity.startDate)) {
            sr.addValidationMessage(EntityId.lessonTemplates, "endDate", "common.students.validation.relative.mobile.not.defined")
        }

        return sr
    }


    fun getAge(date: LocalDate): Double {
        val now = DateTimeUtils.currentDate()
        return Period.between(date, now).toTotalMonths().toDouble() / 12
    }

    fun validateName(name: String?): ParametrizedMessage? {
        return validateByChain(name, listOf(
                { name -> validateNotBlank(name) },
                { name -> validateMaxLength(name, NAME_MAX_LENGTH) }
        ))
    }

    fun validateNotNegativeNumber(value: Int): ParametrizedMessage? {
        if (value < 0) {
            return ParametrizedMessage("common.validation.not.negative")
        }
        return null
    }

    fun validateMaxLength(value: String?, maxLength: Int): ParametrizedMessage? {
        if (value != null && value.length > maxLength) {
            return ParametrizedMessage("common.validation.field.max.length", maxLength)
        }
        return null
    }

    fun validateNotBlank(value: String?): ParametrizedMessage? {
        if (value.isNullOrBlank()) {
            return ParametrizedMessage("common.validation.field.not.empty")
        }
        return null
    }

    fun validateDefined(value: Any?): ParametrizedMessage? {
        if (value == null) {
            return ParametrizedMessage("common.validation.field.not.empty")
        }
        return null
    }

    fun validateEmail(email: String?): ParametrizedMessage? {
        if (email.isNullOrBlank()) {
            return null
        }
        val emailRegex = Regex("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        if (email != null && !emailRegex.matches(email)) {
            return ParametrizedMessage("common.validation.mail.not.valid")
        }
        return null
    }

    fun <T> validateByChain(obj: T, fns: List<(T) -> ParametrizedMessage?>): ParametrizedMessage? {
        if (fns.isEmpty()) {
            return null
        }
        var message: ParametrizedMessage? = null
        fns.forEach {
            message = it(obj)
            if (message != null) {
                return message
            }
        }
        return message
    }

    fun select(condition: Boolean, message: String): ParametrizedMessage? {
        return if (condition) ParametrizedMessage(message) else null
    }

    fun select(condition: Boolean, message: ParametrizedMessage): ParametrizedMessage? {
        return if (condition) message else null
    }

}