package com.joins.kidcenter.domain

import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.EnumUtils
import java.time.LocalDate
import javax.persistence.*

@Entity
class Card : Persistable {
    @Id
    @SequenceGenerator(name = "card_gen", sequenceName = "card_seq", allocationSize = 1)
    @GeneratedValue(generator = "card_gen")
    override var id: Long? = null
    @Enumerated(EnumType.STRING)
    var ageRange: AgeRange = AgeRange.r2_7
    var creationDate: LocalDate = DateTimeUtils.currentDate()
    var expirationDate: LocalDate = DateTimeUtils.currentDate().plusYears(1)
    var active: Boolean = true
    var price: Int = 0
    var maxDiscount: Int = 0
    /**
     * Max possible count of cards to sell
     */
    var maxSalesCount: Int = 100
    var allowedSubjectsMask: Int = EnumUtils.defaultBitmask(LessonSubject.values()) //all lessons allowed
    var visitType: VisitType = VisitType.regular
    var durationDays: Int = 0
    var durationDaysMax: Int = 0
    var lessonsLimit: Int = 0
    var cancelsLimit: Int = 0
    @Column(name = "late_cancels_limit")
    var lateCancelsLimit: Int = 0
    @Column(name = "last_moment_cancels_limit")
    var lastMomentCancelsLimit: Int = 0
    @Column(name = "undue_cancels_limit")
    var undueCancelsLimit: Int = 0
    @Column(name = "miss_limit")
    var missLimit: Int = 0
    var suspendsLimit: Int = 0
    //calculated
    @Column(name = "sold_count")
    var soldCount: Int = 0
}


enum class VisitType(val chargeless: Boolean, val forMembers: Boolean, val canBeTransferred: Boolean) {
    regular(false, true, true),
    trial(false, false, false),
    bonus(true, false, false),
    transfer(false, true, false)
}
