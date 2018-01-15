package com.joins.kidcenter.repository

import com.joins.kidcenter.domain.*
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

interface StudentRepository : CrudRepository<Student, Long> {

    @Query(nativeQuery = true, value = "select nextval('trial_id_seq')")
    fun selectNextTrialId(): Long

    @Query(nativeQuery = true, value = "select nextval('regular_id_seq')")
    fun selectNextRegularId(): Long

    @Query(nativeQuery = true, value = "select id, businessid, nameen, namecn from student where promoter = :referenceAndName")
    fun findPromotedStudents(@Param("referenceAndName") promoterId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select count(id) from student where promoter = :referenceAndName")
    fun findPromotedStudentsCount(@Param("referenceAndName") promoterId: Long): BigDecimal

    @Query(nativeQuery = true, value = "select id, businessid, trialbusinessid from student where status = :studentStatus and (trim(both ' ' from businessid) = '' or trim(both ' ' from trialbusinessid) = '')")
    fun findInStatusWithEmptyBusinessId(@Param("studentStatus") studentStatus: Int): List<Array<Any>>

    @Query(nativeQuery = true, value = "select id, businessid, trialbusinessid from student where status != :studentStatus and (trim(both ' ' from businessid) = '' or trim(both ' ' from trialbusinessid) = '')")
    fun findNotInStatusWithEmptyBusinessId(@Param("studentStatus") studentStatus: Int): List<Array<Any>>

    @Modifying
    @Query("update Student s set s.businessId=:businessId, s.trialBusinessId=:trialBusinessId where s.id=:id")
    fun updateBusinessIds(@Param("id") id: Long, @Param("businessId") businessId: String, @Param("trialBusinessId") trialBusinessId: String): Int

    @Query(nativeQuery = true, value = "select s.id from student s join student_studentrelative sr on sr.student_id=s.id join studentrelative r on sr.relatives_id=r.id where r.mobile like :text or r.mail like :text or r.name like :text")
    fun findIdsByRelativeMobile(@Param("text") text: String): List<BigInteger>

    @Query(nativeQuery = true, value = "select id from student")
    fun findIds(): List<BigInteger>

    fun findByFamilyInAndIdNot(families: Collection<StudentFamily>, id: Long): List<Student>
}

interface StudentRelativeRepository : CrudRepository<StudentRelative, Long>
interface StudentRelativeRoleRepository : CrudRepository<StudentRelativeRole, Long> {
    fun findByName(name: String): List<StudentRelativeRole>
}

interface StudentFamilyRepository : CrudRepository<StudentFamily, Long>
interface StudentCardRepository : CrudRepository<StudentCard, Long> {
    @Query(nativeQuery = true, value = "select id from studentcard")
    fun findIds(): List<BigInteger>

    @Query(nativeQuery = true, value = "select visittype, sum(lessonslimit) ll, sum(lessonsavailable) la, sum(lessonslimit-lessonsavailable) lu, sum(cancelslimit-cancelsavailable) cu, sum(suspendslimit-suspendsavailable) su from studentcard where student_id=:studentId group by visittype")
    fun findStudentLessonsUsage(@Param("studentId") studentId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select visittype, sum(lessonsavailable) la from studentcard where student_id=:studentId and (activationdate isnull or activationdate + durationdays >= :date) group by visittype")
    fun findNotExpiredAvailableLessonsByVisitType(@Param("studentId") studentId: Long, @Param("date") date: Date): List<Array<Any>>

    @Query(nativeQuery = true, value = "select visittype, sum(lessonslimit) ll from studentcard where student_id=:studentId group by visittype")
    fun findStudentLessonLimits(@Param("studentId") studentId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select max(activationdate + durationdays) from studentcard where student_id = :studentId")
    fun findLastCardValidDate(@Param("studentId") studentId: Long): Date?

    @Query(nativeQuery = true, value = "select count(*) from studentcard where student_id = :studentId and visittype = :visitType")
    fun findCardsCountByVisitType(@Param("studentId") studentId: Long, @Param("visitType") visitType: Int): Long

    fun findBySourceTransferCardAndActivationDateNullOrderByIdAsc(card: StudentCard): List<StudentCard>
}

interface StudentCallRepository : CrudRepository<StudentCall, Long>

interface KinderGardenRepository : CrudRepository<KinderGarden, Long>
interface CardRepository : CrudRepository<Card, Long> {
    @Query(nativeQuery = true, value = "select count(sc.id) from studentcard sc where sc.card_id = :cardId")
    fun findStudentCardsCountByCard(@Param("cardId") cardId: Long): BigDecimal
}

interface LessonTemplateRepository : CrudRepository<LessonTemplate, Long> {
    @Query(nativeQuery = true, value = "select * from LessonTemplate lt where lt.startDate <= :endDate and lt.endDate >= :startDate order by startDate asc")
    fun findBetweenDates(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<LessonTemplate>
}

interface LessonSlotRepository : CrudRepository<LessonSlot, String> {
    @Query(nativeQuery = true, value = "select * from LessonSlot ls where ls.dateTime >= :startDate and ls.dateTime < :endDate order by dateTime")
    fun findForDates(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<LessonSlot>

    @Query(nativeQuery = true, value = "select * from LessonSlot ls join StudentSlot ss on ls.id = ss.lessonslot_id where ss.student_id = :studentId and ls.dateTime >= :startDate and ls.dateTime < :endDate and ss.status = :status order by dateTime")
    fun findForStudentDatesAndStatus(@Param("studentId") studentId: Long, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, @Param("status") status: Int): List<LessonSlot>

    @Query(nativeQuery = true, value = "select distinct s.id from student s join studentslot ss on ss.student_id=s.id join lessonslot ls on ls.id = ss.lessonslot_id where ls.dateTime >= :startDate and ls.dateTime < :endDate")
    fun findStudentIdsForDates(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Long>

    @Query(nativeQuery = true, value = "select distinct s.id from student s join studentslot ss on ss.student_id=s.id join lessonslot ls on ls.id = ss.lessonslot_id where ls.dateTime >= :startDate and ls.dateTime < :endDate and ss.status = :status")
    fun findStudentIdsForDatesAndStatus(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, @Param("status") status: Int): List<Long>

    @Query(nativeQuery = true, value = "select ls.id, ls.status, " +
            "(select count(ss.id) from studentslot ss where ss.lessonslot_id=ls.id and ss.visittype=0) as reg_student_count, " + //regular students count for each lesson slot, do not change visitType enum
            "(select count(ss.id) from studentslot ss where ss.lessonslot_id=ls.id) as any_student_count, " + //all students count for each lesson slot
            "(select count(ss.id) from studentslot ss where ss.lessonslot_id=ls.id and ss.student_id=:studentId) as specific_student_count " + //specific students found by id count for each lesson to prevent plan lesson for same student to different cards
            "from LessonSlot ls where ls.id in (:ids)")
    fun findSummaryForIds(@Param("ids") ids: Collection<String>, @Param("studentId") studentId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select substring(ss.lessonslot_id, 2) as ltime, count(ss.id) from studentslot ss where substring(ss.lessonslot_id, 2) in (:ids) and ss.student_id=:studentId and ss.status not in (:statuses) group by ltime")
    fun findLessonsTimesCountByIdsNotInStatus(@Param("ids") ids: Collection<String>, @Param("studentId") studentId: Long, @Param("statuses") statuses: List<Int>): List<Array<Any>>

    fun findByDateTimeAndStatusAndSubjectIn(dateTime: LocalDateTime, status: LessonSlotStatus, subjects: Collection<LessonSubject>): List<LessonSlot>

    fun findByDateTimeLessThanAndStatus(dateTime: LocalDateTime, status: LessonSlotStatus): List<LessonSlot>
}

interface StudentSlotRepository : CrudRepository<StudentSlot, Long> {

    @Query(nativeQuery = true, value = "delete from studentslot ss where ss.student_id=:studentId and ss.lessonslot_id in (:lessonIds) and ss.status=:status")
    @Modifying
    fun deleteSlotsByStudentInLessonsInStatus(@Param("studentId") studentId: Long, @Param("lessonIds") lessonIds: Collection<String>, @Param("status") status: Int): Int

    @Query(nativeQuery = true, value = "update studentslot ss set repeatsleft=1 where ss.card_id=:cardId and ss.status in (:statuses)")
    @Modifying
    fun clearRepeatsLeftByCardAndStatuses(@Param("cardId") cardId: Long, @Param("statuses") statuses: List<Int>): Int

    @Query(nativeQuery = true, value = "update studentslot ss set repeatsleft=:repeatsLeft where ss.id=:slotId")
    @Modifying
    fun updateRepeatsLeftInSlot(@Param("slotId") slotId: Long, @Param("repeatsLeft") repeatsLeft: Int): Int

    @Query(nativeQuery = true, value = "select ss.id, ss.lessonslot_id from studentslot ss join lessonslot ls on ss.lessonslot_id=ls.id where ss.card_id=:cardId and ss.status not in :excludedStatuses and ls.datetime > :date")
    fun findByCardAfterDateNotInStatus(@Param("cardId") cardId: Long, @Param("date") date: LocalDate, @Param("excludedStatuses") excludedStatuses: List<Int>): List<Array<Any>>

    @Query(nativeQuery = true, value = "select * from studentslot where lessonslot_id=:lessonId and card_id=:studentCardId")
    fun findByLessonAndCard(@Param("lessonId") lessonId: String, @Param("studentCardId") studentCardId: Long): StudentSlot?

    @Query(nativeQuery = true, value = "select ls.id lsId, ss.id ssId, ss.status, ss.cancel_type from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.card_id=:cardId and ls.datetime in (:dateTimes) and ss.status=:status")
    fun findLessonsAndSlotsByStudentCardInDateTimeInStatus(@Param("cardId") cardId: Long, @Param("dateTimes") dateTimes: Collection<Timestamp>, @Param("status") status: Int): List<Array<Any>>

    @Query(nativeQuery = true, value = "select status, visittype, count(id) from studentslot where student_id=:studentId group by status, visittype")
    fun findStudentSlotsCountGroupedByVisitTypeAndStatus(@Param("studentId") studentId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select status, count(id) from studentslot where student_id=:studentId group by status")
    fun findStudentSlotsCountGroupedByStatus(@Param("studentId") studentId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select status, count(id) from studentslot where card_id=:cardId group by status")
    fun findStudentSlotsCountByCardGroupedByStatus(@Param("cardId") cardId: Long): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.id ssid, ls.id lsid, ls.agegroup, ss.visittype, ss.repeatsleft, ss.status ssstatus, ss.cancel_type, ss.invalidated, ss.modified_date, ss.modified_by from studentslot ss inner join lessonslot ls on ss.lessonslot_id = ls.id where ss.student_id=:studentId and visitType in (:visitTypes) and ss.status in (:statuses) and ls.datetime > :date order by ls.datetime asc")
    fun findStudentLessonsSchedule(@Param("studentId") studentId: Long, @Param("visitTypes") visitTypes: List<Int>, @Param("statuses") statuses: List<Int>, @Param("date") date: Date): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.id ssid, ls.id lsid, ls.agegroup, ss.visittype, ss.repeatsleft, ss.status ssstatus, ss.cancel_type, ss.invalidated, ss.modified_date, ss.modified_by from studentslot ss inner join lessonslot ls on ss.lessonslot_id = ls.id where ss.student_id=:studentId and visitType in (:visitTypes) and ss.status in (:statuses) and ls.datetime < :date order by ls.datetime desc")
    fun findStudentLessonsHistory(@Param("studentId") studentId: Long, @Param("visitTypes") visitTypes: List<Int>, @Param("statuses") statuses: List<Int>, @Param("date") date: Date): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id, ss.student_id from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.student_id in (:studentIds) and ls.datetime >= :startDate and ls.datetime < :endDate and ss.status = :status order by ls.datetime asc")
    fun findLessonIdsForStudentsByStatusInRangeOrderedByDateAsc(@Param("studentIds") studentIds: Set<Long>, @Param("startDate") startDate: Date, @Param("endDate") endDate: Date, @Param("status") status: Int): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id from studentslot ss where ss.student_id = :studentId and ss.lessonslot_id in (:lessonIds) and ss.status = :status")
    fun findStudentLessonIdsInLessonIdsInStatus(@Param("studentId") studentId: Long, @Param("lessonIds") lessonIds: Collection<String>, @Param("status") status: Int): List<String>

    @Query(nativeQuery = true, value = "select ss.id from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ls.datetime < :date and ss.status = :status")
    fun findSlotIdsByStatusBeforeDate(@Param("date") date: Timestamp, @Param("status") status: Int): List<BigInteger>

    @Query(nativeQuery = true, value = "select max(ls.datetime) from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.student_id = :studentId and ss.status = :status")
    fun findStudentLatestLessonDateByStatus(@Param("studentId") studentId: Long, @Param("status") status: Int): Timestamp?

    @Query(nativeQuery = true, value = "select ss.lessonslot_id, ss.id, ss.status from studentslot ss where ss.repeatsleft>1 and ss.card_id=:cardId and status not in (:statuses)")
    fun findRepeatableStudentSlotsByCardAndNotInStatus(@Param("cardId") cardId: Long, @Param("statuses") statuses: List<Int>): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id, ss.id, ss.status, ss.cancel_type from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.card_id=:cardId and ss.status not in (:statuses) and ls.datetime >= :datetime and (ls.subject || '-' || extract(isodow from ls.datetime)-1 || '-' || lpad(extract(hour from ls.datetime) || '', 2, '0') || ':' || lpad(extract(minute from ls.datetime) || '', 2, '0')) in (:relativeLessonIds)")
    fun findStudentSlotsWithRepeatedInLessonsIdNotInStatusFromDateTime(@Param("cardId") cardId: Long, @Param("relativeLessonIds") relativeLessonIds: List<String>, @Param("statuses") statuses: List<Int>, @Param("datetime") datetime: Timestamp): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id, ss.id, ss.status, ss.cancel_type from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.card_id=:cardId and ls.datetime >= :datetime and (ls.subject || '-' || extract(isodow from ls.datetime)-1 || '-' || lpad(extract(hour from ls.datetime) || '', 2, '0') || ':' || lpad(extract(minute from ls.datetime) || '', 2, '0')) in (:relativeLessonIds)")
    fun findStudentSlotsWithRepeatedInLessonsIdFromDateTime(@Param("cardId") cardId: Long, @Param("relativeLessonIds") relativeLessonIds: List<String>, @Param("datetime") datetime: Timestamp): List<Array<Any>>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id from studentslot ss join lessonslot ls on ss.lessonslot_id = ls.id where ss.student_id=:studentId and ss.status in (:statuses)")
    fun findStudentLessonIdsBySlotStatusIn(@Param("studentId") studentId: Long, @Param("statuses") statuses: List<Int>): List<String>

    @Query(nativeQuery = true, value = "select ss.lessonslot_id, ss.id from studentslot ss where ss.card_id=:studentCardId and status in (:statuses)")
    fun findStudentCardLessonIdsAndSlotIdsBySlotStatusIn(@Param("studentCardId") studentCardId: Long, @Param("statuses") statuses: List<Int>): List<Array<Any>>

    @Modifying
    @Query(nativeQuery = true, value = "delete from studentslot where id in (:ids)")
    fun deleteByIdIn(@Param("ids") ids: Collection<Long>): Unit
}

interface TeacherRepository : CrudRepository<Teacher, Long>

interface PaymentRepository : CrudRepository<Payment, Long> {

    @Query(nativeQuery = true, value = "select target_account_id, target_school_id, date_trunc('month', date), sum(price) from payment where target_account_id in (:accountIds) and target_school_id in (:schoolIds) group by target_account_id, target_school_id, date_trunc('month', date)")
    fun findMonthIncomesByPeriod(@Param("accountIds") accountIds: Collection<Long>, @Param("schoolIds") schoolIds: Collection<Long>): List<Array<Any>>

    @Query(nativeQuery = true, value = "select account_id, school_id, date_trunc('month', date), sum(price) from payment where account_id in (:accountIds) and school_id in (:schoolIds) group by account_id, school_id, date_trunc('month', date)")
    fun findMonthExpensesByPeriod(@Param("accountIds") accountIds: Collection<Long>, @Param("schoolIds") schoolIds: Collection<Long>): List<Array<Any>>

    @Query(nativeQuery = true, value = "select account_id, school_id, sum(price) from payment where account_id in (:accountIds) and school_id in (:schoolIds) and date >= :startDate and date <= :endDate group by account_id, school_id")
    fun findExpenseByPeriod(@Param("accountIds") accountIds: Collection<Long>, @Param("schoolIds") schoolIds: Collection<Long>, @Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<Array<Any>>

    @Query(nativeQuery = true, value = "select target_account_id, target_school_id, sum(price) from payment where target_account_id in (:accountIds) and target_school_id in (:schoolIds) and date >= :startDate and date <= :endDate group by target_account_id, target_school_id")
    fun findIncomeByPeriod(@Param("accountIds") accountIds: Collection<Long>, @Param("schoolIds") schoolIds: Collection<Long>, @Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<Array<Any>>

    @Query(nativeQuery = true, value = "select p.id, s.businessid from payment p join studentcard st on st.payment_id = p.id join student s on st.student_id = s.id where p.id in (:paymentIds)")
    fun findStudentBusinessIds(@Param("paymentIds") paymentIds: Collection<Long>): List<Array<Any>>
}

interface AccountRepository : CrudRepository<Account, Long> {
    @Query(nativeQuery = true, value = "select nextval('account_seq')")
    fun selectNextAccountId(): Long

    @Query(nativeQuery = true, value = "select * from account order by id")
    fun findOrderById(): List<Account>

    fun findByBusinessId(businessId: String): List<Account>

    @Query(nativeQuery = true, value = "select account_id from account_school where school_id = :schoolId")
    fun findIdsBySchoolId(@Param("schoolId") schoolId: Long): List<Long>

    @Query(nativeQuery = true, value = "select account_id from account_school acs join school s on acs.school_id = s.id where s.external = :external")
    fun findIdsBySchoolExternal(@Param("external") external: Boolean): List<Long>
}

interface SchoolRepository : CrudRepository<School, Long> {
    @Query(nativeQuery = true, value = "select * from school order by id")
    fun findOrderById(): List<School>

    @Query(nativeQuery = true, value = "select id from school where external = :external")
    fun findIdsByExternal(@Param("external") external: Boolean): List<Long>

    @Query(nativeQuery = true, value = "select school_id from account_school where account_id in (:accountIds)")
    fun findIdsByAccountIds(@Param("accountIds") accountIds: Collection<Long>): List<Long>

    @Query(nativeQuery = true, value = "select school_id, account_id from account_school")
    fun findSchoolAccountIdPairs(): List<Array<Any>>
}

interface CityRepository : CrudRepository<City, Long> {
    @Query(nativeQuery = true, value = "select * from city order by id")
    fun findOrderById(): List<City>
}

interface CategoryRepository : CrudRepository<Category, Long> {
    @Query(nativeQuery = true, value = "select * from category order by id")
    fun findOrderById(): List<Category>

    @Query(nativeQuery = true, value = "select id from category where parent_id in (:parentIds)")
    fun findIdsByParentIds(@Param("parentIds") parentIds: Collection<Long>): List<Long>

    @Query(nativeQuery = true, value = "select id from category where lower(name) like :prefix% escape '\\'")
    fun findIdsByNamePrefix(@Param("prefix") prefix: String): List<Long>

    @Modifying
    @Query(nativeQuery = true, value = "update category set level = :level where id in (:ids)")
    fun updateLevelByIds(@Param("ids") ids: Collection<Long>, @Param("level") level: Int)
}

interface HomeworkRepository : CrudRepository<Homework, Long>

interface PromotionSourceRepository : CrudRepository<PromotionSource, Long>

interface PromotionDetailRepository : CrudRepository<PromotionDetail, Long>

interface SchoolVisitRepository : CrudRepository<SchoolVisit, Long> {

    fun findByStudentAndDate(student: Student, date: LocalDate): SchoolVisit?

    @Query(nativeQuery = true, value = "select id, student_id from school_visit where date = :date")
    fun findByDate(@Param("date") date: LocalDate): List<Array<Any>>
}
