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

import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.dto.internal.ParametrizedMessage
import com.joins.kidcenter.dto.preference.StudentCardPaymentPreference
import com.joins.kidcenter.repository.*
import com.joins.kidcenter.service.exceptions.OperationException
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.ValidatorsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.BigInteger
import javax.persistence.EntityManager
import kotlin.reflect.KMutableProperty0

interface StudentCardService : FindOneService<StudentCard>, SaveService<StudentCard, SaveResult<StudentCard>>, DeleteOneService {
    fun updateStudentLessonTotalCounts(studentId: Long)
    fun updateStudentCardLessonTotalCounts(cardId: Long)
    fun updateLastCardValidDate(studentId: Long)
    fun findStudentByCard(cardId: Long): Student?
    fun create(createRequest: StudentCardPaymentRequest): SaveResult<StudentCard>
    fun addPayment(paymentRequest: StudentCardPaymentRequest): SaveResult<StudentCard>
    fun updateLessonTotals()
}

@Service
@Transactional
open class StudentCardServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: StudentCardRepository,
        val cardRepository: CardRepository,
        val studentCardRepository: StudentCardRepository,
        val studentRepository: StudentRepository,
        val studentSlotRepository: StudentSlotRepository,
        val studentStatusManager: StudentStatusManager,
        val preferenceService: PreferenceService,
        val schoolRepository: SchoolRepository,
        var accountService: AccountService,
        val accountRepository: AccountRepository,
        val categoryRepository: CategoryRepository,
        val paymentService: PaymentService
) : EntityService(em, queryBuilder), StudentCardService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): StudentCard? = repository.findOne(id)

    @Transactional(readOnly = true)
    override fun findStudentByCard(cardId: Long): Student? {
        val card = findOne(cardId)
        if (card != null) {
            return card.student
        }
        return null
    }

    override fun create(createRequest: StudentCardPaymentRequest): SaveResult<StudentCard> {
        val studentCard = createRequest.card
        if (studentCard != null) {
            if (studentCard.price > 0) {
                return saveStudentCardAndPayment(validateCreateRequest(createRequest))
            } else {
                return save(studentCard)
            }
        }
        return SaveResult(ValidatedEntity(EntityId.studentCardPayment), Config.commonValidationMessage, ParametrizedMessage("common.error.student.card.card.not.defined"))
    }

    override fun addPayment(paymentRequest: StudentCardPaymentRequest): SaveResult<StudentCard> {
        return saveStudentCardAndPayment(validateAddPaymentRequest(paymentRequest))
    }

    private fun saveStudentCardAndPayment(validationResult: SaveResult<ValidatedStudentCardPaymentRequest>): SaveResult<StudentCard> {
        if (validationResult.hasErrors()) {
            return SaveResult(validationResult.validationMessages)
        }

        val validatedRequest = validationResult.obj!!
        val accountType = validatedRequest.accountType
        val partner = validatedRequest.partner
        val studentCard = validatedRequest.card
        val student = studentCard.student!!
        val studentAccount: Account = accountService.save(student, accountType, partner.id!!)

        val payment = Payment().apply({
            direction = PaymentDirection.incoming
            account = studentAccount
            school = partner
            this.targetAccount = validatedRequest.targetAccount
            this.targetSchool = validatedRequest.targetPartner
            this.category = validatedRequest.category
            this.price = BigDecimal(studentCard.price)
            this.date = studentCard.purchaseDate
        })
        val savedPaymentResult = paymentService.save(payment)
        if (savedPaymentResult.hasErrors()) {
            return SaveResult(savedPaymentResult.validationMessages)
        }
        studentCard.payment = savedPaymentResult.obj

        return save(studentCard)
    }

    private fun validateCreateRequest(request: StudentCardPaymentRequest): SaveResult<ValidatedStudentCardPaymentRequest> {
        val validation = ValidatorsUtil.validateStudentCardCreateRequest(request)
        if (validation.hasErrors()) {
            return SaveResult(validation.validationMessages)
        }

        return validateOtherRequestParams(request.card!!, request)
    }

    private fun validateAddPaymentRequest(request: StudentCardPaymentRequest): SaveResult<ValidatedStudentCardPaymentRequest> {
        val studentCard = repository.findOne(request.card?.id ?: -1)
        if (studentCard == null) {
            return SaveResult(EntityId.studentCardPayment, "common.error.student.card.card.not.found")
        } else if (studentCard.price <= 0) {
            return SaveResult(EntityId.studentCardPayment, "common.error.student.card.card.has.payment")
        } else if (studentCard.payment != null) {
            return SaveResult(EntityId.studentCardPayment, "common.error.student.card.card.has.payment")
        }

        return validateOtherRequestParams(studentCard, request)
    }

    private fun validateOtherRequestParams(studentCard: StudentCard, request: StudentCardPaymentRequest): SaveResult<ValidatedStudentCardPaymentRequest> {
        val student = studentRepository.findOne(studentCard.student!!.id!!)
        if (student == null) {
            return SaveResult(EntityId.studentCardPayment, "common.error.student.card.student.not.found")
        }

        val paymentPreference = preferenceService.getPreference<StudentCardPaymentPreference>(PreferenceTypes.studentCardPayment)
        if (paymentPreference.obj == null) {
            return SaveResult(EntityId.studentCardPayment, "common.error.student.card.payment.preference.not.found")
        } else {
            val partner = schoolRepository.findOne(paymentPreference.obj.partnerId)
            if (partner == null) {
                return SaveResult(EntityId.studentCardPayment, "common.error.student.card.payment.preference.partner.not.found")
            }
            val category = categoryRepository.findOne(paymentPreference.obj.categoryId)
            if (category == null) {
                return SaveResult(EntityId.studentCardPayment, "common.error.student.card.payment.preference.category.not.found")
            }

            val targetAccount = accountRepository.findOne(request.targetAccount?.id ?: -1)
            if (targetAccount == null) {
                return SaveResult(EntityId.studentCardPayment, "common.error.student.card.target.account.not.found")
            }

            val targetPartner = schoolRepository.findOne(request.targetPartner?.id ?: -1)
            if (targetPartner == null) {
                return SaveResult(EntityId.studentCardPayment, "common.error.student.card.target.partner.not.found")
            }

            val accountType = request.accountType!!
            val validatedStudentCardCreateRequest = ValidatedStudentCardPaymentRequest(studentCard, category, partner, accountType, targetPartner, targetAccount)
            return SaveResult(validatedStudentCardCreateRequest)
        }
    }

    override fun save(entity: StudentCard): SaveResult<StudentCard> {
        entity.activationDate = entity.purchaseDate //explicit bound activation to purchase date

        val validation = ValidatorsUtil.validateStudentCard(entity, EntityId.studentCards)
        if (validation.hasErrors()) {
            return validation
        }
        val prevEntity = if (entity.isNew()) null else repository.findOne(entity.id)
        updateLimits(entity, prevEntity)
        updateStudentStatus(entity)
        val saveResult = SaveResult(repository.save(entity))
        em.flush()

        val studentId = entity.student!!.id!!
        updateStudentLessonTotalCounts(studentId)
        updateLastCardValidDate(studentId)
        updateCardSoldCount(entity.cardId)
        return saveResult
    }

    override fun updateStudentLessonTotalCounts(studentId: Long) {
        val student = studentRepository.findOne(studentId)
        if (student != null) {
            var studentLessonsCount = 0
            var studentPaidLessonsCount = 0
            var studentBonusLessonsCount = 0
            repository.findStudentLessonLimits(studentId).map {
                val visitType = VisitType.values()[it[0] as Int]
                val lessonsCount = (it[1] as BigInteger).toInt()
                if (visitType != VisitType.trial) {
                    studentLessonsCount += lessonsCount
                }
                if (visitType.forMembers) { //Lesson paid by long term contract
                    studentPaidLessonsCount += lessonsCount
                }
                if (visitType.chargeless) {
                    studentBonusLessonsCount += lessonsCount
                }
            }

            student.paidLessonsCount = studentPaidLessonsCount
            student.bonusLessonsCount = studentBonusLessonsCount

            val studentSlotCounts: MutableMap<StudentSlotStatus, Long> = mutableMapOf()
            studentSlotRepository.findStudentSlotsCountGroupedByVisitTypeAndStatus(student.id!!).forEach {
                val studentSlotStatus = StudentSlotStatus.values()[it[0] as Int]
                val visitType = VisitType.values()[it[1] as Int]
                val slotCount = (it[2] as BigInteger).toLong()
                if (visitType != VisitType.trial) {
                    studentSlotCounts[studentSlotStatus] = slotCount
                }
            }
            student.plannedLessonsCount = studentSlotCounts[StudentSlotStatus.planned]?.toInt() ?: 0
            student.visitedLessonsCount = studentSlotCounts[StudentSlotStatus.visited]?.toInt() ?: 0
            val missedLessonsCount = studentSlotCounts[StudentSlotStatus.missed]?.toInt() ?: 0
            student.usedLessonsCount = student.visitedLessonsCount + student.plannedLessonsCount + missedLessonsCount
            student.availableLessonsCount = studentLessonsCount - student.usedLessonsCount
            studentRepository.save(student)
        }
    }

    private fun updateCardSoldCount(cardId: Long?) {
        if (cardId != null) {
            val card = cardRepository.findOne(cardId)
            if (card != null) {
                card.soldCount = cardRepository.findStudentCardsCountByCard(cardId).toInt()
                cardRepository.save(card)
            }
        }
    }

    override fun updateStudentCardLessonTotalCounts(cardId: Long) {
        val studentCard = studentCardRepository.findOne(cardId)
        if (studentCard != null) {

            val studentSlotCounts: MutableMap<StudentSlotStatus, Long> = mutableMapOf()
            studentSlotRepository.findStudentSlotsCountByCardGroupedByStatus(studentCard.id!!).forEach {
                val studentSlotStatus = StudentSlotStatus.values()[it[0] as Int]
                val slotCount = (it[1] as BigInteger).toLong()
                studentSlotCounts[studentSlotStatus] = slotCount
            }
            studentCard.plannedLessonsCount = studentSlotCounts[StudentSlotStatus.planned]?.toInt() ?: 0
            studentCard.spentLessonsCount = (studentSlotCounts[StudentSlotStatus.visited]?.toInt() ?: 0) +
                    (studentSlotCounts[StudentSlotStatus.missed]?.toInt() ?: 0)

            studentCardRepository.save(studentCard)
        }
    }

    override fun updateLastCardValidDate(studentId: Long) {
        val student = studentRepository.findOne(studentId)
        if (student != null) {
            student.lastCardValidDate = repository.findLastCardValidDate(studentId)?.toLocalDate()
            studentRepository.save(student)
        }
    }


    private fun updateLimits(entity: StudentCard, prevEntity: StudentCard?) {
        if (prevEntity != null) {
            if (prevEntity.durationDays > entity.durationDays) {
                entity.durationDays = prevEntity.durationDays
            }
            tryShiftPropertyValue(entity::lessonsLimit, prevEntity::lessonsLimit, entity::lessonsAvailable, prevEntity::lessonsAvailable)
            tryShiftPropertyValue(entity::cancelsLimit, prevEntity::cancelsLimit, entity::cancelsAvailable, prevEntity::cancelsAvailable)
            tryShiftPropertyValue(entity::lateCancelsLimit, prevEntity::lateCancelsLimit, entity::lateCancelsAvailable, prevEntity::lateCancelsAvailable)
            tryShiftPropertyValue(entity::lastMomentCancelsLimit, prevEntity::lastMomentCancelsLimit, entity::lastMomentCancelsAvailable, prevEntity::lastMomentCancelsAvailable)
            tryShiftPropertyValue(entity::undueCancelsLimit, prevEntity::undueCancelsLimit, entity::undueCancelsAvailable, prevEntity::undueCancelsAvailable)
            tryShiftPropertyValue(entity::missLimit, prevEntity::missLimit, entity::missAvailable, prevEntity::missAvailable)
            tryShiftPropertyValue(entity::suspendsLimit, prevEntity::suspendsLimit, entity::suspendsAvailable, prevEntity::suspendsAvailable)
        }
    }

    private fun tryShiftPropertyValue(limitProp: KMutableProperty0<Int>, previousLimitProp: KMutableProperty0<Int>, availableProp: KMutableProperty0<Int>, previousAvailableProp: KMutableProperty0<Int>) {
        val diff = limitProp.get() - previousLimitProp.get()
        availableProp.set(previousAvailableProp.get() + diff)
    }

    private fun updateStudentStatus(entity: StudentCard) {
        entity.student = studentRepository.findOne(entity.student!!.id)
        studentStatusManager.updateOnCardSell(entity)
    }

    override fun delete(id: Long): DeleteResult<Long> {
        val studentCard = repository.findOne(id)
        if (studentCard != null) {
            val payment = studentCard.payment
            val sourceTransferCard = studentCard.sourceTransferCard

            if (studentCard.student != null) {
                studentCard.student!!.cards.remove(studentCard)
            }
            val studentId = studentCard.student!!.id!!
            repository.delete(studentCard)
            em.flush()

            if (payment != null) {
                val result = paymentService.delete(payment.id!!)
                if (!result.response.success()) {
                    throw OperationException(result, HttpStatus.CONFLICT)
                }
            }

            if (sourceTransferCard != null) {
                sourceTransferCard.lessonsLimit += studentCard.lessonsLimit
                sourceTransferCard.lessonsAvailable += studentCard.lessonsAvailable
                repository.save(sourceTransferCard)
            }

            updateStudentLessonTotalCounts(studentId)
            updateLastCardValidDate(studentId)
            updateCardSoldCount(studentCard.cardId)
            studentStatusManager.updateOnCardRemove(studentCard)
        }
        return DeleteResult(id)
    }

    override fun updateLessonTotals() {
        repository.findIds().map { it.toLong() }.forEach {
            updateStudentCardLessonTotalCounts(it)
        }
    }
}
