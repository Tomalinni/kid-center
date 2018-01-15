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

package com.joins.kidcenter.service.maps

import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.Payment
import com.joins.kidcenter.dto.AccountPeriodBalance
import com.joins.kidcenter.dto.BalanceResult
import com.joins.kidcenter.dto.DateRange
import com.joins.kidcenter.repository.AccountRepository
import com.joins.kidcenter.repository.PaymentRepository
import com.joins.kidcenter.repository.SchoolRepository
import com.joins.kidcenter.utils.DateTimeUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct

data class AccountSchoolMonthKey(val accountId: Long,
                                 val schoolId: Long,
                                 val monthDate: LocalDate)

data class AccountSchool(val accountId: Long,
                         val schoolId: Long)

class MonthSum(val balance: BigDecimal,
               val expense: BigDecimal,
               val income: BigDecimal) {
    fun withBalance(balance: BigDecimal) =
            MonthSum(balance, this.expense, this.income)

    fun withExpense(expense: BigDecimal) =
            MonthSum(this.balance, expense, this.income)

    fun withIncome(income: BigDecimal) =
            MonthSum(this.balance, this.expense, income)

    companion object {
        fun zero() = MonthSum(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }
}

interface SumModifier {
    fun modify(sum: MonthSum): MonthSum
}

class ExpenseModifier(val diff: BigDecimal) : SumModifier {
    override fun modify(sum: MonthSum): MonthSum = sum.withExpense(sum.expense + diff)
}

class IncomeModifier(val diff: BigDecimal) : SumModifier {
    override fun modify(sum: MonthSum): MonthSum = sum.withIncome(sum.income + diff)
}

@Component
open class AccountSchoolMonthStorage @Autowired constructor(val paymentRepository: PaymentRepository,
                                                            val accountRepository: AccountRepository,
                                                            val schoolRepository: SchoolRepository) {
    val log: Logger = LoggerFactory.getLogger(AccountSchoolMonthStorage::class.java)

    private val lock = ReentrantLock(true)
    private val sums: MutableMap<AccountSchoolMonthKey, MonthSum> = ConcurrentHashMap()
    private val dates: MutableMap<AccountSchool, DateRange> = ConcurrentHashMap()

    @PostConstruct
    private fun init() {
        refreshAll()

    }

    @Scheduled(fixedDelay = Config.paymentMonthSumRefreshPeriod)
    private fun refreshAll() {
        refresh(accountRepository.findIdsBySchoolExternal(false), schoolRepository.findIdsByExternal(false))
    }

    fun refresh(accountIds: Collection<Long>, schoolIds: Collection<Long>) {
        log.info("Payment month sums are being refreshed.")
        if (accountIds.isNotEmpty() && schoolIds.isNotEmpty()) {
            val incomesByPeriod = paymentRepository.findMonthIncomesByPeriod(accountIds, schoolIds)
                    .map({ mapSum(it) }).toMap()
            val expensesByPeriod = paymentRepository.findMonthExpensesByPeriod(accountIds, schoolIds)
                    .map({ mapSum(it) }).toMap()

            doWithLock {
                hashSetOf<AccountSchoolMonthKey>().plus(incomesByPeriod.keys).plus(expensesByPeriod.keys)
                        .forEach {
                            val key = AccountSchool(it.accountId, it.schoolId)
                            var dateRange = dates[key]
                            if (dateRange == null) {
                                dateRange = DateRange(it.monthDate, it.monthDate)
                            } else {
                                dateRange = dateRange.withMinStartDate(it.monthDate)
                                dateRange = dateRange.withMaxEndDate(it.monthDate)
                            }
                            dates[key] = dateRange

                            sums[it] = MonthSum(BigDecimal.ZERO, expensesByPeriod[it] ?: BigDecimal.ZERO, incomesByPeriod[it] ?: BigDecimal.ZERO)
                        }

                accountIds.forEach { accountId ->
                    schoolIds.forEach { schoolId ->
                        val dateRange = dates[AccountSchool(accountId, schoolId)]

                        if (dateRange != null) {
                            var date = dateRange.startDate
                            while (!date.isAfter(dateRange.endDate)) {
                                val prevAccountMonthSum = sums[AccountSchoolMonthKey(accountId, schoolId, date.minusMonths(1))]
                                val prevBalance = if (prevAccountMonthSum != null) prevAccountMonthSum.balance + prevAccountMonthSum.income - prevAccountMonthSum.expense else BigDecimal.ZERO
                                val accountMonthKey = AccountSchoolMonthKey(accountId, schoolId, date)
                                val savedAccountMonthSum = sums[accountMonthKey]
                                val accountMonthSum = MonthSum(prevBalance, savedAccountMonthSum?.expense ?: BigDecimal.ZERO, savedAccountMonthSum?.income ?: BigDecimal.ZERO)
                                sums[accountMonthKey] = accountMonthSum

                                date = date!!.plusMonths(1)
                            }
                        }

                    }
                }
            }
        }
    }

    fun applyPaymentChange(oldPayment: Payment?, newPayment: Payment?) {
        val oldSourceAccountId = oldPayment?.account?.id
        val oldTargetAccountId = oldPayment?.targetAccount?.id
        val oldSourceSchoolId = oldPayment?.school?.id
        val oldTargetSchoolId = oldPayment?.targetSchool?.id
        val oldMonth = if (oldPayment != null) oldPayment.date.withDayOfMonth(1) else null
        val oldPrice = oldPayment?.price ?: BigDecimal.ZERO

        val newSourceAccountId = newPayment?.account?.id
        val newTargetAccountId = newPayment?.targetAccount?.id
        val newSourceSchoolId = newPayment?.school?.id
        val newTargetSchoolId = newPayment?.targetSchool?.id
        val newMonth = if (newPayment != null) newPayment.date.withDayOfMonth(1) else null
        val newPrice = newPayment?.price ?: BigDecimal.ZERO

        doWithLock {
            tryApplySumChange(oldSourceAccountId, oldSourceSchoolId, oldMonth, ExpenseModifier(-oldPrice))
            tryApplySumChange(oldTargetAccountId, oldTargetSchoolId, oldMonth, IncomeModifier(-oldPrice))
            tryApplySumChange(newSourceAccountId, newSourceSchoolId, newMonth, ExpenseModifier(newPrice))
            tryApplySumChange(newTargetAccountId, newTargetSchoolId, newMonth, IncomeModifier(newPrice))
        }
    }

    private fun tryApplySumChange(accountId: Long?, schoolId: Long?, startMonth: LocalDate?, sumModifier: SumModifier) {
        if (accountId != null && schoolId != null && startMonth != null) {
            val key = AccountSchoolMonthKey(accountId, schoolId, startMonth)
            var sum = sums[key]

            if (sum == null) {
                sum = MonthSum.zero()
            }
            sum = sumModifier.modify(sum)
            sums[key] = sum

            propagateBalanceChange(accountId, schoolId, startMonth, sum)
        }
    }


    private fun mapSum(it: Array<Any>): Pair<AccountSchoolMonthKey, BigDecimal> {
        val accountId = (it[0] as BigInteger).toLong()
        val schoolId = (it[1] as BigInteger).toLong()
        val monthDate = (it[2] as Timestamp).toLocalDateTime().toLocalDate()
        val sum = it[3] as BigDecimal
        return Pair(AccountSchoolMonthKey(accountId, schoolId, monthDate), sum)
    }

    fun get(dateRange: DateRange, accountIds: Collection<Long>, schoolIds: Collection<Long>): BalanceResult {
        val startDate: LocalDate = dateRange.startDate
        val endDate: LocalDate = dateRange.endDate
        val result: MutableList<AccountPeriodBalance> = mutableListOf()
        if (!startDate.isAfter(endDate) && accountIds.isNotEmpty() && schoolIds.isNotEmpty()) {

            doWithLock {
                accountIds.forEach { accountId ->
                    schoolIds.forEach { schoolId ->
                        val existingRange = dates[AccountSchool(accountId, schoolId)]
                        if (existingRange != null) {
                            result.add(getBalance(accountId, schoolId, startDate, endDate, existingRange))
                        }
                    }
                }
            }
        }
        return BalanceResult(result)
    }

    private fun getBalance(accountId: Long, schoolId: Long, startDate: LocalDate, endDate: LocalDate, existingRange: DateRange): AccountPeriodBalance {
        if (endDate.isBefore(existingRange.startDate)) {
            return AccountPeriodBalance.flat(accountId, schoolId, BigDecimal.ZERO)
        } else if (startDate.isAfter(existingRange.endDate)) {
            val sum = sums[AccountSchoolMonthKey(accountId, schoolId, existingRange.endDate)]
            val endBalnce = sum!!.balance + sum.income - sum.expense
            return AccountPeriodBalance.flat(accountId, schoolId, endBalnce)
        } else {
            val actualStartDate = DateTimeUtils.max(startDate, existingRange.startDate)
            val actualEndDate = DateTimeUtils.min(endDate, existingRange.endDate)
            return getSeveralMonthsBalance(accountId, schoolId, actualStartDate, actualEndDate)
        }
    }

    private fun propagateBalanceChange(accountId: Long, schoolId: Long, startDate: LocalDate, sum: MonthSum) {
        val dateKey = AccountSchool(accountId, schoolId)
        val existingRange = dates[dateKey]
        var changedSum = sum
        var curDate = startDate

        if (existingRange != null) {
            while (curDate.isBefore(existingRange.endDate)) {
                curDate = curDate.plusMonths(1)
                val key = AccountSchoolMonthKey(accountId, schoolId, curDate)
                var curSum = sums[key]
                if (curSum == null) {
                    curSum = MonthSum.zero()
                }
                curSum = curSum.withBalance(changedSum!!.balance + changedSum.income - changedSum.expense)
                sums[key] = curSum
                changedSum = curSum
            }
            val extendedRange = existingRange.withMaxEndDate(startDate).withMinStartDate(startDate)
            dates[dateKey] = extendedRange
        } else {
            dates[dateKey] = DateRange.singleDate(startDate)
        }
    }

    private fun getSeveralMonthsBalance(accountId: Long, schoolId: Long, startDate: LocalDate, endDate: LocalDate): AccountPeriodBalance {
        var curDate = startDate
        var startBalance = BigDecimal.ZERO
        var startBalanceDefined = false
        var income = BigDecimal.ZERO
        var expense = BigDecimal.ZERO

        while (!curDate.isAfter(endDate)) {
            val accountMonthSum = sums[AccountSchoolMonthKey(accountId, schoolId, curDate)]
            if (accountMonthSum != null) {
                if (!startBalanceDefined) {
                    startBalance = accountMonthSum.balance
                    startBalanceDefined = true
                }
                income += accountMonthSum.income
                expense += accountMonthSum.expense
            }

            curDate = curDate.plusMonths(1)
        }
        val endBalance = startBalance + income - expense
        val balance = AccountPeriodBalance(accountId, schoolId, startBalance, endBalance, income, expense)
        return balance
    }


    private fun <T> doWithLock(action: () -> T): T {
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }

}