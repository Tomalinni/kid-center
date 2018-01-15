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
import java.math.BigDecimal
import java.time.LocalDate

class PaymentDto {
    var id: Long = 0
    var direction: PaymentDirection = PaymentDirection.outgoing
    var account: AccountDto? = null
    var targetAccount: AccountDto? = null
    var school: SchoolDto? = null
    var targetSchool: SchoolDto? = null
    var date: LocalDate = DateTimeUtils.currentDate()
    var monthDate: LocalDate? = null
    var category: EntityNameRef<Long>? = null
    var category2: EntityNameRef<Long>? = null
    var category3: EntityNameRef<Long>? = null
    var category4: EntityNameRef<Long>? = null
    var category5: EntityNameRef<Long>? = null
    var price: Double = 0.0
    var comment: String = ""
    var productUrl: String = ""
    var receiptPhotosCount: Int = 0
    var productPhotosCount: Int = 0
    var receiptPhotos: Collection<String> = emptyList()
    var productPhotos: Collection<String> = emptyList()
    var studentBusinessId: String? = null

    companion object {
        fun fromDomainObject(payment: Payment): PaymentDto {
            return PaymentDto().apply {
                id = payment.id!!
                direction = payment.direction
                account = if (payment.account != null) AccountDto.fromDomainObject(payment.account!!) else null
                targetAccount = if (payment.targetAccount != null) AccountDto.fromDomainObject(payment.targetAccount!!) else null
                school = if (payment.school != null) SchoolDto.fromDomainObject(payment.school!!) else null
                targetSchool = if (payment.targetSchool != null) SchoolDto.fromDomainObject(payment.targetSchool!!) else null
                date = payment.date
                monthDate = payment.monthDate
                category = if (payment.category != null) EntityNameRef.fromDomainObject(payment.category!!) else null
                category2 = if (payment.category2 != null) EntityNameRef.fromDomainObject(payment.category2!!) else null
                category3 = if (payment.category3 != null) EntityNameRef.fromDomainObject(payment.category3!!) else null
                category4 = if (payment.category4 != null) EntityNameRef.fromDomainObject(payment.category4!!) else null
                category5 = if (payment.category5 != null) EntityNameRef.fromDomainObject(payment.category5!!) else null
                price = payment.price.toDouble()
                comment = payment.comment
                productUrl = payment.productUrl
                receiptPhotosCount = payment.receiptPhotosCount
                productPhotosCount = payment.productPhotosCount
            }
        }
    }
}

class PaymentStatItem(val category: Long,
                      val monthDate: LocalDate,
                      val sum: BigDecimal) {

    fun isValid(): Boolean = category != 0L && monthDate != LocalDate.MIN
}

class AccountDto {
    var id: Long = 0
    var type: AccountType = AccountType.cashless
    var schools: List<SchoolDto> = listOf()
    var city: CityDto? = null
    var bank: String = ""
    var department: String = ""
    var owner: String = ""
    var ownerAbbr: String = ""
    var number: String = ""
    var login: String = ""
    var name: String = ""

    companion object {
        fun fromDomainObject(account: Account): AccountDto {
            return AccountDto().apply {
                id = account.id!!
                type = account.type
                schools = account.schools.map({ SchoolDto.fromDomainObject(it) }).toList()
                city = if (account.city == null) null else CityDto.fromDomainObject(account.city!!)
                bank = account.bank
                department = account.department
                owner = account.owner
                number = account.number
                login = account.login
                name = getName(account)
                ownerAbbr = getOwnerAbbr(account)
            }
        }

        fun getName(account: Account?): String {
            if (account == null) return ""
            if (account.type == AccountType.cashless) {
                return getCashLessName(account)
            } else {
                return getDefaultName(account)
            }
        }

        private fun getCashLessName(account: Account): String {
            val cityLetter = account.city?.name?.take(1) ?: ""
            val bankLetter = account.bank.take(1)
            val departmentLetter = account.department.take(1)
            val numberSuffix = account.number.takeLast(4)
            return "$cityLetter$bankLetter$departmentLetter${getOwnerAbbr(account)}$numberSuffix"
        }

        private fun getDefaultName(account: Account): String {
            val loginAbbr = account.login.take(4)
            return "${getOwnerAbbr(account)}$loginAbbr"
        }

        private fun getOwnerAbbr(account: Account): String {
            val ownerAbbr: String
            val parts: List<String> = account.owner.trim().split(" ")
            if (parts.size == 1) {
                val part = parts[0]
                if (part.length <= 1) {
                    ownerAbbr = part
                } else {
                    ownerAbbr = "${part[0]}${part[part.lastIndex]}"
                }
            } else {
                ownerAbbr = "${parts[0][0]}${parts[1][0]}"
            }
            return ownerAbbr.toUpperCase()
        }
    }
}

class SchoolDto {
    var id: Long = 0
    var name: String = ""
    var city: CityDto? = null
    var external: Boolean = false

    companion object {
        fun fromDomainObject(school: School): SchoolDto {
            return SchoolDto().apply {
                id = school.id!!
                name = school.name
                city = if (school.city == null) null else CityDto.fromDomainObject(school.city!!)
                external = school.external
            }
        }
    }
}

class CityDto {
    var id: Long = 0
    var name: String = ""

    companion object {
        fun fromDomainObject(city: City): CityDto {
            return CityDto().apply {
                id = city.id!!
                name = city.name
            }
        }
    }
}

open class CategoryDto {
    var id: Long = 0
    var name: String = ""
    var level: Int = 0
    var hasTargetMonth: Boolean = false
    var parent: EntityRef<Long>? = null
    val children: MutableList<EntityRef<Long>> = mutableListOf()

    companion object {
        fun fromDomainObject(category: Category): CategoryDto {
            val parentCategory = category.parent
            return CategoryDto().apply {
                id = category.id!!
                name = category.name
                level = category.level
                hasTargetMonth = category.hasTargetMonth
                parent = if (parentCategory != null) EntityRef(parentCategory.id!!) else null
            }
        }
    }
}

class DictionariesDto {
    var cities: List<DictionaryItemDto> = listOf()
    var schools: List<SchoolDto> = listOf()
    var accounts: List<AccountDto> = listOf()
    var categories: Map<Long, CategoryDto> = mapOf()
    var relativeRoles: List<StudentRelativeRole> = listOf()
    var roles: List<RoleDto> = listOf()
    var employees: List<DictionaryItemDto> = listOf()
    var promotionSources: List<PromotionSourceDto> = listOf()
    var promotionDetails: List<PromotionDetailDto> = listOf()
}

class DictionaryItemDto {
    var id: Long = 0
    var name: String = ""

    companion object {
        fun fromDomainObject(city: City): DictionaryItemDto {
            return DictionaryItemDto().apply {
                id = city.id!!
                name = city.name
            }
        }

        fun fromDomainObject(employee: Teacher): DictionaryItemDto {
            return DictionaryItemDto().apply {
                id = employee.id!!
                name = employee.name
            }
        }
    }
}


class LineChartResult {
    var series: List<LineChartSerie> = listOf()
}

class LineChartSerie {
    var name: String = ""
    var data: List<BigDecimal> = listOf()
}

class AccountPeriodBalance(val accountId: Long = 0,
                           val schoolId: Long = 0,
                           val startBalance: BigDecimal = BigDecimal.ZERO,
                           val endBalance: BigDecimal = BigDecimal.ZERO,
                           val income: BigDecimal = BigDecimal.ZERO,
                           val expense: BigDecimal = BigDecimal.ZERO) {
    companion object {
        fun flat(accountId: Long = 0, schoolId: Long = 0, balance: BigDecimal = BigDecimal.ZERO): AccountPeriodBalance
                = AccountPeriodBalance(accountId, schoolId, balance, balance, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}

class BalanceResult(var items: List<AccountPeriodBalance>)

class AccountSchoolSum(val schoolId: Long = 0,
                       val accountId: Long = 0,
                       val sum: BigDecimal = BigDecimal.ZERO) {
}

class AccountSchoolBalance(val schoolId: Long = 0,
                           val accountId: Long = 0,
                           var expense: BigDecimal = BigDecimal.ZERO,
                           var income: BigDecimal = BigDecimal.ZERO) {

    fun plus(balance: AccountSchoolBalance) = AccountSchoolBalance(this.schoolId, this.accountId, this.expense.add(balance.expense), this.income.add(balance.income))

    companion object {
        fun schoolZero(schoolId: Long) = AccountSchoolBalance(schoolId, 0, BigDecimal.ZERO, BigDecimal.ZERO)

        fun accountZero(accountId: Long) = AccountSchoolBalance(0, accountId, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}
