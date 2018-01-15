package com.joins.kidcenter.service

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.domain.StudentStatus.registered
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.dto.lessons.LessonSlotId
import com.joins.kidcenter.repository.*
import com.joins.kidcenter.service.maps.MobileConfirmationService
import com.joins.kidcenter.service.maps.StudentNextLessonsProvider
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.service.persistence.StudentCodeGenerator
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.criteria.*

interface StudentService : FindOneService<Student>, SaveService<Student, SaveResult<Student>>, DeleteOneService {
    fun findAll(searchRequest: StudentSearchRequest): SearchResult<Student>
    fun findDashboardData(id: Long): StudentDashboardDto?
    fun findStudentLessons(studentId: Long, visitTypes: Array<VisitType>, statuses: Array<StudentSlotStatus>, lessonDate: LocalDate, timeCategory: StudentLessonsTimeCategory): List<StudentDashboardLesson>
    fun findRelativesWithMobileNotification(id: Long, notification: MobileNotification): List<StudentRelative>
    fun findDashboardLessons(searchRequest: StudentLessonsSearchRequest): Map<String, List<StudentDashboardLesson>>
    fun fixBusinessIds()
    fun saveRelativesNotifications(request: StudentRelativesDto): Student?
    fun updateLessonTotals()
}

@Service
@Transactional
open class StudentServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: StudentRepository,
        val codeGenerator: StudentCodeGenerator,
        val studentRelativeRepository: StudentRelativeRepository,
        val studentFamilyRepository: StudentFamilyRepository,
        val studentCardRepository: StudentCardRepository,
        val studentSlotRepository: StudentSlotRepository,
        val schoolVisitRepository: SchoolVisitRepository,
        val confirmationService: MobileConfirmationService,
        val nextLessonsProvider: StudentNextLessonsProvider,
        val storageService: FileStorageServiceImpl,
        val studentCardService: StudentCardService
) : EntityService(em, queryBuilder), StudentService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Student? = mergeWithNextLessons(mergeWithSiblings(listOf(repository.findOne(id))))[0]

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: StudentSearchRequest): SearchResult<Student> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1

            val studentIds = findExtraStudentIds(searchRequest)
            val predicateFactory = StudentFiltersPredicateFactory(em, searchRequest, studentIds)
            val orderFactory = StudentOrderFactoryImpl(em, searchRequest)
            val listQuery = listQuery(predicateFactory, orderFactory)
            val countQuery = countQuery(predicateFactory)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = if (results.size == maxResults || firstResult > 0)
                em.createQuery(countQuery).singleResult as Long
            else
                results.size.toLong()

            @Suppress("UNCHECKED_CAST")
            val students = (results as Iterable<Student>).toList()
            return SearchResult(mergeWithNextLessons(students), total)
        } else {
            return SearchResult(listOf<Student>(), 0)
        }
    }

    private fun findExtraStudentIds(searchRequest: StudentSearchRequest): List<Long> {
        if (searchRequest.text.isNotBlank()) {
            return repository.findIdsByRelativeMobile("%${searchRequest.text}%").map { it.toLong() }
        }
        return listOf()
    }

    private fun mergeWithNextLessons(students: List<Student>): List<Student> {
        if (students.isNotEmpty()) {
            val studentsMap = students.map { Pair(it.id!!, it) }.toMap()
            nextLessonsProvider.getForStudents(studentsMap.keys).forEach { entry ->
                val student = studentsMap[entry.key]
                if (student != null) {
                    student.nextLessons = entry.value
                }
            }
            return studentsMap.values.toList()
        }
        return students
    }

    private fun mergeWithSiblings(students: List<Student>): List<Student> {
        if (students.isNotEmpty()) {
            students.forEach { student ->
                if (student.family != null) {
                    val siblings = repository.findByFamilyInAndIdNot(listOf(student.family!!), student.id!!)
                    student.siblings = siblings
                }
            }
        }
        return students
    }

    private fun listQuery(predicateFactory: ListPredicateFactory<Student>, orderFactory: StudentOrderFactoryImpl): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Student::class.java)
        return criteria.select(from).where(predicateFactory.createPredicate(from)).orderBy(*orderFactory.create(from))
    }

    private fun countQuery(predicateFactory: ListPredicateFactory<Student>): CriteriaQuery<Any> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        val criteria = cb.createQuery()
        val from = criteria.from(Student::class.java)
        return criteria.select(cb.count(from)).where(predicateFactory.createPredicate(from))
    }

    @Transactional(readOnly = true)
    override fun findDashboardData(id: Long): StudentDashboardDto? {
        val student = repository.findOne(id)
        if (student != null) {
            mergeWithSiblings(listOf(student))
            val lessonSums: MutableMap<VisitType, StudentDashboardLessonTypeSummary> = mutableMapOf()
            val notUsedlessonsCount: MutableMap<VisitType, Int> = mutableMapOf()
            lessonSums.putAll(VisitType.values().map { Pair(it, StudentDashboardLessonTypeSummary()) }.toMap())
            notUsedlessonsCount.putAll(VisitType.values().map { Pair(it, 0) }.toMap())

            studentCardRepository.findStudentLessonsUsage(id).forEach {
                val visitType = VisitType.values()[it[0] as Int]
                val lessonsTotal = (it[1] as BigInteger).toInt()
                val lessonsNotUsed = (it[2] as BigInteger).toInt()
                val lessonsUsed = (it[3] as BigInteger).toInt()
                val cancelsUsed = (it[4] as BigInteger).toInt()
                val suspendsUsed = (it[5] as BigInteger).toInt()
                lessonSums[visitType]?.total = lessonsTotal
                lessonSums[visitType]?.used = lessonsUsed
                lessonSums[visitType]?.canceled = cancelsUsed
                lessonSums[visitType]?.suspended = suspendsUsed
                notUsedlessonsCount[visitType] = lessonsNotUsed
            }
            studentCardRepository.findNotExpiredAvailableLessonsByVisitType(id, Date(System.currentTimeMillis())).forEach {
                val visitType = VisitType.values()[it[0] as Int]
                val lessonsAvailable = (it[1] as BigInteger).toInt()
                lessonSums[visitType]?.available = lessonsAvailable
                lessonSums[visitType]?.expired = notUsedlessonsCount[visitType]?.minus(lessonsAvailable) ?: 0
            }
            studentSlotRepository.findStudentSlotsCountGroupedByVisitTypeAndStatus(id).forEach {
                val status = StudentSlotStatus.values()[it[0] as Int]
                val visitType = VisitType.values()[it[1] as Int]
                val count = (it[2] as BigInteger).toInt()
                lessonSums[visitType]?.apply {
                    when (status) {
                        StudentSlotStatus.planned -> planned = count
                        StudentSlotStatus.visited -> visited = count
                        StudentSlotStatus.missed -> missed = count
                        else -> { /*nothing*/
                        }
                    }
                }
            }
            val promotedStudents = repository.findPromotedStudents(id).map {
                StudentNamesDto().apply {
                    this.id = (it[0] as BigInteger).toLong()
                    this.businessId = it[1] as String
                    this.nameEn = it[2] as String
                    this.nameCn = it[3] as String
                }
            }

            val studentPresentInSchool = schoolVisitRepository.findByStudentAndDate(student, DateTimeUtils.currentDate()) != null

            return StudentDashboardDto().apply {
                this.id = student.id!!
                businessId = student.businessId
                trialBusinessId = student.trialBusinessId
                status = student.status
                nameEn = student.nameEn
                nameCn = student.nameCn
                birthDate = student.birthDate
                gender = student.gender
                kinderGarden = student.kinderGarden
                comment = student.comment
                lessonsSummary.putAll(lessonSums)
                cards.addAll(student.cards.map { StudentDashboardStudentCardDto.fromDomainObject(it) })
                relatives.addAll(student.relatives)
                promoter = if (student.promoter != null) StudentNamesDto.fromDomainObject(student.promoter!!) else null
                this.promotedStudents.addAll(promotedStudents)
                this.promotionSource = if (student.promotionSource == null) null else PromotionSourceDto.fromDomainObject(student.promotionSource!!)
                this.promotionDetail = if (student.promotionDetail == null) null else PromotionDetailDto.fromDomainObject(student.promotionDetail!!)
                if (!student.primaryPhotoName.isNullOrEmpty()) {
                    photoName = student.primaryPhotoName
                } else {
                    val photoNames: List<String> = storageService.providers().student(student.id!!).listNames()
                    photoName = if (photoNames.isNotEmpty()) photoNames[0] else null
                }
                presentInSchool = studentPresentInSchool
                siblings = student.siblings.map { StudentNamesDto.fromDomainObject(it) }.toMutableList()
            }
        }
        return null
    }

    override fun findDashboardLessons(searchRequest: StudentLessonsSearchRequest): Map<String, List<StudentDashboardLesson>> {
        val visitTypes = if (searchRequest.visitType != null) arrayOf(searchRequest.visitType!!) else VisitType.values()
        val statuses = StudentSlotStatus.values()

        return findStudentLessons(searchRequest.id, visitTypes, statuses, searchRequest.lessonDate, searchRequest.timeCategory)
                .groupBy { DateTimeUtils.dateToString(LessonSlotId.fromString(it.lessonId).dateTime.toLocalDate()) }
    }

    override fun findStudentLessons(studentId: Long, visitTypes: Array<VisitType>, statuses: Array<StudentSlotStatus>, lessonDate: LocalDate, timeCategory: StudentLessonsTimeCategory): List<StudentDashboardLesson> {
        val studentLessonsCols: List<Array<Any>>
        val requestedVisitTypes = EnumUtils.ordinals(visitTypes)
        val requestedStatuses = EnumUtils.ordinals(statuses)
        val lessonSqlDate = lessonDate.toSqlDate()

        if (timeCategory == StudentLessonsTimeCategory.schedule) {
            studentLessonsCols = studentSlotRepository.findStudentLessonsSchedule(studentId, requestedVisitTypes, requestedStatuses, lessonSqlDate)
        } else {
            studentLessonsCols = studentSlotRepository.findStudentLessonsHistory(studentId, requestedVisitTypes, requestedStatuses, lessonSqlDate)
        }

        return studentLessonsCols.map {
            val id = (it[0] as BigInteger).toLong()
            val lessonId = it[1] as String
            val ageGroup = LessonAgeGroup.values()[it[2] as Int]
            val visitType = VisitType.values()[it[3] as Int]
            val repeatsLeft = it[4] as Int
            val status = StudentSlotStatus.values()[it[5] as Int]
            val cancelTypeOrdinal = it[6] as Int?
            val cancelType = if (cancelTypeOrdinal != null) LessonCancelType.values()[cancelTypeOrdinal] else null
            val invalidated = it[7] as Boolean
            val modifiedDateTime = (it[8] as Timestamp).toLocalDateTime()
            val modifiedBy = it[9] as String
            StudentDashboardLesson(id, visitType, repeatsLeft, status, cancelType, invalidated, lessonId, ageGroup, modifiedDateTime, modifiedBy)
        }
    }

    override fun findRelativesWithMobileNotification(id: Long, notification: MobileNotification): List<StudentRelative> {
        val relatives: MutableList<StudentRelative> = mutableListOf()
        val student = findOne(id)
        if (student != null) {
            student.relatives.forEach {
                if (!it.mobile.isNullOrBlank() && it.mobileConfirmed && it.mobileNotifications.contains(notification)) {
                    relatives.add(it)
                }
            }
        }
        return relatives
    }

    override fun save(entity: Student): SaveResult<Student> {
        val validation = ValidatorsUtil.validateStudent(entity)
        if (validation.hasErrors()) {
            return validation
        }
        val prevEntity = if (entity.isNew()) null else repository.findOne(entity.id)
        if (prevEntity != null) {
            mergeWithSiblings(listOf(prevEntity))
        }
        updatePrimaryRelative(entity)
        updateStudentCards(entity, prevEntity)
        processMobileConfirmation(entity, prevEntity, validation)

        val promoter = refresh(entity.promoter)
        val prevPromoter = refresh(prevEntity?.promoter)
        if (validation.hasErrors()) {
            return validation
        }

        processSiblings(entity, prevEntity)
        updateStatusAndBusinessId(entity, prevEntity)
        val student = repository.save(entity)
        em.flush()

        updatePromotedStudentCount(promoter, prevPromoter)
        return SaveResult(student)
    }

    /**
     * Processing logic.
     *
     * Added siblings:
     * Find all related siblings
     * Choose family to set
     * Set family to siblings
     * Set relatives to siblings
     *
     * Removed siblings:
     * If any, clone relatives and set to current student and rest siblings.
     * Preserve original relatives on removed siblings.
     */
    private fun processSiblings(entity: Student, prevEntity: Student?) {
        val prevSiblings: Map<Long, Student> = prevEntity?.siblings?.map { Pair(it.id!!, it) }?.toMap() ?: emptyMap()
        val prevRelatives: Map<Long, StudentRelative> = prevEntity?.relatives?.map { Pair(it.id!!, it) }?.toMap() ?: emptyMap()
        val siblingIds: List<Long> = entity.siblings.filter { it.id != null }.map { it.id!! }
        val siblings: Map<Long, Student> = if (siblingIds.isNotEmpty()) repository.findAll(siblingIds).map { Pair(it.id!!, it) }.toMap() else emptyMap<Long, Student>()
        val relatives: Map<Long, StudentRelative> = entity.relatives.filter { it.id != null }.map { Pair(it.id!!, it) }.toMap()
        val removedRelatives = prevRelatives.minus(relatives.keys)

        val addedSiblings: Map<Long, Student> = siblings.minus(prevSiblings.keys)
        val removedSiblings: Map<Long, Student> = prevSiblings.minus(siblings.keys)
        val addedSiblingFamilies: Collection<StudentFamily> = addedSiblings.values.filter { it.family != null }.map { it.family!! }
        val relatedAddedSiblings: Map<Long, Student> = if (addedSiblingFamilies.isNotEmpty()) repository.findByFamilyInAndIdNot(addedSiblingFamilies, entity.safeId()).map { Pair(it.id!!, it) }.toMap() else addedSiblings
        val relatedSiblings = siblings.plus(relatedAddedSiblings)
        val removedSiblingRelativeIds: Set<Long> = removedSiblings.values.flatMap { it.relatives }.map { it.id!! }.toSet()

        var familyToSet = if (entity.family != null) entity.family
        else if (addedSiblingFamilies.isNotEmpty()) addedSiblingFamilies.elementAt(0)
        else StudentFamily()

        if (familyToSet!!.id == null) {
            familyToSet = studentFamilyRepository.save(familyToSet)
        }
        entity.family = if (siblings.isEmpty()) null else familyToSet

        val relativesToSet: List<StudentRelative> = entity.relatives.map { relative ->
            if (relative.id != null && removedSiblingRelativeIds.contains(relative.id!!)) {
                val clonedRelative = relative.clone()
                clonedRelative.id = null
                return@map clonedRelative
            }
            return@map relative
        }.map { relative ->
            relative.family = familyToSet
            return@map studentRelativeRepository.save(relative)!!
        }
        entity.relatives.clear()
        entity.relatives.addAll(relativesToSet)

        relatedSiblings.values.forEach { sibling ->
            sibling.family = familyToSet
            sibling.relatives.clear()
            sibling.relatives.addAll(relativesToSet)
            repository.save(sibling)
        }

        removedSiblings.values.forEach { sibling ->
            sibling.family = null
            repository.save(sibling)
        }

        studentRelativeRepository.delete(removedRelatives.values)
    }

    private fun updateStatusAndBusinessId(entity: Student, prevEntity: Student?) {
        //explicitly set status to prevent its malicious change from request
        entity.status = prevEntity?.status ?: registered

        if (prevEntity == null) {
            val trialId = codeGenerator.nextTrialId()
            entity.businessId = trialId
            entity.trialBusinessId = trialId
        }
    }

    private fun updateStudentCards(entity: Student, prevEntity: Student?) {
        if (prevEntity != null) {
            entity.cards = prevEntity.cards //cards are unmodifiable during student saving
        }
    }

    private fun updatePromotedStudentCount(promoter: Student?, prevPromoter: Student?) {
        val promoterId = promoter?.id
        val prevPromoterId = prevPromoter?.id

        if (prevPromoterId != promoterId) {
            if (prevPromoterId != null) {
                prevPromoter.promotedStudentCount = repository.findPromotedStudentsCount(prevPromoterId).toInt()
                repository.save(prevPromoter)
            }

            if (promoterId != null) {
                promoter.promotedStudentCount = repository.findPromotedStudentsCount(promoterId).toInt()
                repository.save(promoter)
            }
        }
    }

    private fun updatePrimaryRelative(entity: Student) {
        val relativesWithIds = entity.relatives.filter { it.id != null }
        if (!relativesWithIds.isEmpty()) {
            entity.primaryRelativeName = relativesWithIds.minBy { it.id!! }?.name ?: ""
        } else if (!entity.relatives.isEmpty()) {
            entity.primaryRelativeName = entity.relatives[0].name ?: ""
        } else {
            entity.primaryRelativeName = ""
        }
    }


    private fun processMobileConfirmation(entity: Student, prevEntity: Student?, validation: SaveResult<Student>) {
        val relativesById = entity.relatives.mapIndexed { i, r -> Pair(r.id ?: -i, IndexedEntity(i, r)) }.toMap()
        @Suppress("IfThenToElvis")
        val prevRelativesById = if (prevEntity == null) emptyMap<String, IndexedEntity<StudentRelative>>() else prevEntity.relatives.mapIndexed { i, r -> Pair(r.id, IndexedEntity(i, r)) }.toMap()
        val ids = setOf<String>().plus(relativesById.keys).plus(prevRelativesById.keys)
        ids.forEach {
            processRelativeMobileConfirmation(relativesById[it], prevRelativesById[it])
        }
        propagateMobileConfirmedStatusToOtherRelatives(entity)
        entity.mobileConfirmed = entity.relatives.any { it.mobileConfirmed }
    }

    private fun processRelativeMobileConfirmation(relative: IndexedEntity<StudentRelative>?, prevRelative: IndexedEntity<StudentRelative>?) {
        if (relative != null) {
            //prevRelative == null && relative.id != null is valid case when user adds previously created relative to student. It`s assumed that mobile phone verification had been done when relative was created.
            val prevMobile = prevRelative?.obj?.mobile ?: ""
            val mobile = relative.obj.mobile ?: ""

            val mobileChanged = mobile != prevMobile
            val mobileWasNotConfirmed = !(prevRelative?.obj?.mobileConfirmed ?: false)
            val mobileNotBlank = !mobile.isBlank()
            val confirmationCodeNotBlank = !relative.obj.confirmationCode.isBlank()

            if ((mobileChanged || mobileWasNotConfirmed) && mobileNotBlank && confirmationCodeNotBlank) {
                val checkResult = confirmationService.checkConfirmation(relative.obj.confirmationId ?: "", mobile, relative.obj.confirmationCode)

                relative.obj.mobileConfirmed = checkResult.error == null
            }
        }
    }

    /**
     * Propagates mobile confirmation status to other student relatives, if they have the same phone.
     * Works only in scope of single student, as working with several possibly sibling students is a security risk.
     * Known confirmed phone can be used to register malicious client.
     */
    private fun propagateMobileConfirmedStatusToOtherRelatives(entity: Student) {
        val confirmedPhones: MutableSet<String> = mutableSetOf()
        //collect confirm status
        entity.relatives.forEach {
            if (!it.mobile.isNullOrBlank() && it.mobileConfirmed) {
                confirmedPhones.add(it.mobile!!)
            }
        }

        //propagate confirm status
        entity.relatives.forEach {
            if (!it.mobile.isNullOrBlank() && !it.mobileConfirmed && confirmedPhones.contains(it.mobile)) {
                it.mobileConfirmed = true
            }
        }
    }

    override fun delete(id: Long): DeleteResult<Long> {
        val student = repository.findOne(id)
        val promoter = refresh(student.promoter)

        repository.delete(id)
        em.flush()

        if (promoter != null) {
            promoter.promotedStudentCount = repository.findPromotedStudentsCount(promoter.id!!).toInt()
            repository.save(promoter)
        }

        return DeleteResult(id)
    }

    override fun saveRelativesNotifications(request: StudentRelativesDto): Student? {
        val student = findOne(request.studentId)
        if (student != null) {
            student.relatives.forEach {
                val relativeById = request.relativeById(it.id)
                if (relativeById != null) {
                    it.emailNotifications = relativeById.emailNotifications
                    it.mobileNotifications = relativeById.mobileNotifications
                }
            }
            save(student)
        }
        return student
    }

    private fun refresh(student: Student?): Student? {
        return if (student != null && student.id != null) repository.findOne(student.id!!) else null
    }

    override fun fixBusinessIds() {
        fixTrialStudentsBusinessIds()
        fixRegularStudentsBusinessIds()
    }

    private fun fixTrialStudentsBusinessIds() {
        repository.findNotInStatusWithEmptyBusinessId(StudentStatus.cardPaid.ordinal).forEach {
            val id = (it[0] as BigInteger).toLong()
            val businessId = it[1] as String
            val trialBusinessId = it[1] as String

            if (businessId.isBlank() && trialBusinessId.isBlank()) {
                val trialId = codeGenerator.nextTrialId()
                //for trial students trialBusinessId is equal to regularBusinessId
                repository.updateBusinessIds(id, trialId, trialId)
            } else if (!businessId.isBlank() && trialBusinessId.isBlank()) {
                repository.updateBusinessIds(id, businessId, businessId)
            } else {
                repository.updateBusinessIds(id, trialBusinessId, trialBusinessId)
            }
        }
    }

    private fun fixRegularStudentsBusinessIds() {
        repository.findInStatusWithEmptyBusinessId(StudentStatus.cardPaid.ordinal).forEach {
            val id = (it[0] as BigInteger).toLong()
            val businessId = it[1] as String
            val trialBusinessId = it[1] as String

            if (businessId.isBlank() && trialBusinessId.isBlank()) {
                val trialId = codeGenerator.nextTrialId()
                val regularId = codeGenerator.nextRegularId()
                //for regular students trialBusinessId is different from regularBusinessId
                repository.updateBusinessIds(id, regularId, trialId)
            } else if (!businessId.isBlank() && trialBusinessId.isBlank()) {
                val trialId = codeGenerator.nextTrialId()
                repository.updateBusinessIds(id, businessId, trialId)
            } else if (businessId.isBlank() && !trialBusinessId.isBlank()) {
                val regularId = codeGenerator.nextTrialId()
                repository.updateBusinessIds(id, regularId, trialBusinessId)
            }
        }
    }

    override fun updateLessonTotals() {
        repository.findIds().map { it.toLong() }.forEach {
            studentCardService.updateStudentLessonTotalCounts(it)
            studentCardService.updateLastCardValidDate(it)
        }
        studentCardService.updateLessonTotals()
    }
}

class StudentFiltersPredicateFactory(
        val em: EntityManager,
        val data: StudentSearchRequest,
        val studentIds: List<Long>) : ListPredicateFactory<Student> {

    override fun createPredicate(from: Root<Student>): Predicate {
        val cb: CriteriaBuilder = em.criteriaBuilder
        if (data.selection != null) {
            return cb.equal(from.get<Long>("id"), data.selection!!.id)

        } else {
            val textPredicate = PredicateUtil.fieldsContainClause(from, data.text, listOf("businessId", "trialBusinessId", "nameEn", "nameCn"), cb, 1)
            val startDate = data.getEffectiveStartDate().toLocalDateTimeMidnight()
            val endDate = data.getEffectiveEndDate().toLocalDateTimeLastDayMoment()
            val datePredicate = cb.and(cb.greaterThanOrEqualTo(from.get("createdDate"), startDate), cb.lessThanOrEqualTo(from.get("createdDate"), endDate))
            val statusPredicate = if (data.status == null) cb.trueClause() else cb.equal(from.get<StudentStatus>("status"), data.status)
            val managerPredicate = if (data.manager == null) cb.trueClause() else cb.equal(from.get<Teacher>("manager").get<Long>("id"), data.manager)
            val idsPredicate = if (studentIds.isEmpty()) cb.falseClause() else from.get<Long>("id").`in`(studentIds)

            return cb.and(cb.or(textPredicate, idsPredicate), datePredicate, statusPredicate, managerPredicate)
        }
    }
}

class StudentOrderFactoryImpl(
        val em: EntityManager,
        val data: StudentSearchRequest) : OrderFactory<Student> {

    override fun create(from: Root<Student>): Array<Order> {
        val cb: CriteriaBuilder = em.criteriaBuilder
        if (data.sortColumn != null) {
            val orderField = from.get<Any>(data.sortColumn)
            return arrayOf(data.sortOrder.selectOrder(cb, orderField))
        }
        return arrayOf(cb.desc(from.get<Any>("businessId")))

    }
}

