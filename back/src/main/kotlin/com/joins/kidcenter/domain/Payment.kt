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

package com.joins.kidcenter.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.joins.kidcenter.dto.PaymentDirection
import com.joins.kidcenter.utils.DateTimeUtils
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
class Payment() : Persistable {
    @Id
    @SequenceGenerator(name = "payment_gen", sequenceName = "payment_seq", allocationSize = 1)
    @GeneratedValue(generator = "payment_gen")
    override var id: Long? = null

    var date: LocalDate = DateTimeUtils.currentDate()

    var monthDate: LocalDate? = null

    var direction: PaymentDirection = PaymentDirection.outgoing

    @ManyToOne
    @JoinColumn(name = "account_id",
            foreignKey = ForeignKey(name = "payment_account_id_fkey"))
    var account: Account? = null


    @ManyToOne
    @JoinColumn(name = "target_account_id",
            foreignKey = ForeignKey(name = "payment_target_account_id_fkey"))
    var targetAccount: Account? = null

    @ManyToOne
    @JoinColumn(name = "school_id",
            foreignKey = ForeignKey(name = "payment_school_id_fkey"))
    var school: School? = null

    @ManyToOne
    @JoinColumn(name = "target_school_id",
            foreignKey = ForeignKey(name = "payment_target_school_id_fkey"))
    var targetSchool: School? = null

    @ManyToOne
    @JoinColumn(name = "category_id",
            foreignKey = ForeignKey(name = "payment_category_id_fkey"))
    var category: Category? = null

    @ManyToOne
    @JoinColumn(name = "category2_id",
            foreignKey = ForeignKey(name = "payment_category2_id_fkey"))
    var category2: Category? = null

    @ManyToOne
    @JoinColumn(name = "category3_id",
            foreignKey = ForeignKey(name = "payment_category3_id_fkey"))
    var category3: Category? = null

    @ManyToOne
    @JoinColumn(name = "category4_id",
            foreignKey = ForeignKey(name = "payment_category4_id_fkey"))
    var category4: Category? = null

    @ManyToOne
    @JoinColumn(name = "category5_id",
            foreignKey = ForeignKey(name = "payment_category5_id_fkey"))
    var category5: Category? = null

    var price: BigDecimal = BigDecimal.ZERO

    var comment: String = ""

    var productUrl: String = ""

    var receiptPhotosCount: Int = 0

    var productPhotosCount: Int = 0


    companion object {
        @JsonIgnore
        fun relatedAccountIds(cur: Payment?, prev: Payment?) =
                setOf(cur?.account?.id, cur?.targetAccount?.id, prev?.account?.id, prev?.targetAccount?.id).filterNotNull()

        @JsonIgnore
        fun relatedSchoolIds(cur: Payment?, prev: Payment?) =
                setOf(cur?.school?.id, cur?.targetSchool?.id, prev?.school?.id, prev?.targetSchool?.id).filterNotNull()
    }
}

@Entity
class Account : Persistable {
    @Id
    @SequenceGenerator(name = "account_gen", sequenceName = "account_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_gen")
    override var id: Long? = null
    var businessId: String = ""

    var type: AccountType = AccountType.cashless

    @ManyToOne
    var city: City? = null
    var bank: String = ""
    var department: String = ""
    var owner: String = ""
    var login: String = "" //for wechat, alipay, cash types
    var number: String = ""

    @ManyToMany
    @JoinTable(name = "account_school",
            joinColumns = arrayOf(JoinColumn(name = "account_id", referencedColumnName = "id")),
            inverseJoinColumns = arrayOf(JoinColumn(name = "school_id", referencedColumnName = "id")),
            foreignKey = ForeignKey(name = "acc_school_acc_id_fkey"),
            inverseForeignKey = ForeignKey(name = "acc_school_school_id_fkey"))
    var schools: MutableSet<School> = mutableSetOf()
}

enum class AccountType(val abbr: String) {
    cashless("CL"), cash("C"), wechat("W"), alipay("A")
}

@Entity
class School() : Persistable {
    @Id
    @SequenceGenerator(name = "school_gen", sequenceName = "school_seq", allocationSize = 1)
    @GeneratedValue(generator = "school_gen")
    override var id: Long? = null
    var name: String = ""

    @ManyToOne
    var city: City? = null

    val hasLessons: Boolean = true

    val external: Boolean = false
}

@Entity
class City() : Persistable {
    @Id
    @SequenceGenerator(name = "city_gen", sequenceName = "city_seq", allocationSize = 1)
    @GeneratedValue(generator = "city_gen")
    override var id: Long? = null
    var name: String = ""

}

@Entity
class Category() : Persistable {
    @Id
    @SequenceGenerator(name = "category_gen", sequenceName = "category_seq", allocationSize = 1)
    @GeneratedValue(generator = "category_gen")
    override var id: Long? = null

    @JsonIgnore
    @ManyToOne
    var parent: Category? = null

    var hasTargetMonth: Boolean = false
    var level: Int = 0
    var name: String = ""
}