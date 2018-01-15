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

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.dto.AccountDto
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.AccountRepository
import com.joins.kidcenter.repository.SchoolRepository
import com.joins.kidcenter.service.persistence.AccountCodeGenerator
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface AccountService : FindOneService<AccountDto>, SaveService<AccountDto, Account>, DeleteOneService {
    fun findAll(): SearchResult<AccountDto>

    fun save(student: Student, type: AccountType, partnerId: Long): Account
}

@Service
@Transactional
open class AccountServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: AccountRepository,
        val schoolRepository: SchoolRepository,
        val codeGenerator: AccountCodeGenerator
) : EntityService(em, queryBuilder), AccountService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): AccountDto? {
        val account = repository.findOne(id)
        return if (account == null) null else AccountDto.fromDomainObject(account)
    }

    @Transactional(readOnly = true)
    override fun findAll(): SearchResult<AccountDto> {
        @Suppress("UNCHECKED_CAST")
        val accounts = repository.findOrderById()
        return SearchResult(accounts.map { AccountDto.fromDomainObject(it) }, accounts.size.toLong())
    }

    override fun save(entity: AccountDto): Account {
        val account = Account().apply {
            if (entity.id != 0L) {
                id = entity.id
            } else {
                id = repository.selectNextAccountId()
            }
            businessId = codeGenerator.manualAccount(id!!)
            type = entity.type
            schools = entity.schools.map({ School().apply { id = it.id } }).toMutableSet()
            owner = entity.owner
            if (type == AccountType.cashless) {
                city = if (entity.city != null) City().apply { id = entity.city!!.id } else null
                bank = entity.bank
                department = entity.department
                number = entity.number
            } else {
                login = entity.login
            }
        }
        return repository.save(account)
    }

    override fun save(student: Student, type: AccountType, partnerId: Long): Account {
        val studentAccount: Account? = repository.findByBusinessId(codeGenerator.studentAccount(student.id!!, type)).firstOrNull()

        if (studentAccount == null) {
            val partner: School? = schoolRepository.findOne(partnerId)
            val account = Account().apply {
                businessId = codeGenerator.studentAccount(student.id!!, type)
                this.type = type
                schools = mutableSetOf(School().apply { id = partnerId })
                owner = "Student"
                if (type == AccountType.cashless) {
                    city = partner?.city
                    bank = "Bank"
                    department = "Department"
                    number = generateNumberOrLogin(student.businessId, type)
                } else {
                    login = generateNumberOrLogin(student.businessId, type)
                }
            }
            return repository.save(account)

        } else if (studentAccount.businessId != student.businessId) {
            studentAccount.businessId = student.businessId
            if (type == AccountType.cashless) {
                studentAccount.number = generateNumberOrLogin(student.businessId, type)
            } else {
                studentAccount.login = generateNumberOrLogin(student.businessId, type)
            }
            return repository.save(studentAccount)
        }
        return studentAccount
    }

    private fun generateNumberOrLogin(studentBusinessId: String, accountType: AccountType): String {
        return "S${accountType.abbr}$studentBusinessId"
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}