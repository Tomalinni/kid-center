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

import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.*
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface DictionaryService {
    fun load(): DictionariesDto
}

@Service
@Transactional
open class DictionaryServiceImpl @Autowired constructor(
        val cityRepository: CityRepository,
        val schoolRepository: SchoolRepository,
        val accountRepository: AccountRepository,
        val categoryService: CategoryService,
        val studentRelativeRoleRepository: StudentRelativeRoleRepository,
        val roleRepository: AppRoleRepository,
        val promotionSourceRepository: PromotionSourceRepository,
        val promotionDetailRepository: PromotionDetailRepository,
        val teacherRepository: TeacherRepository
) : DictionaryService {

    @Transactional(readOnly = true)
    override fun load(): DictionariesDto {
        val dicts = DictionariesDto()
        if (SecurityUtil.hasAnyPermission(Permission.paymentsRead, Permission.studentCardsModify)) {
            dicts.schools = schoolRepository.findOrderById().map { SchoolDto.fromDomainObject(it) }
            dicts.accounts = accountRepository.findOrderById().map { AccountDto.fromDomainObject(it) }
        }
        if (SecurityUtil.hasPermission(Permission.paymentsRead)) {
            dicts.cities = cityRepository.findOrderById().map { DictionaryItemDto.fromDomainObject(it) }
            dicts.categories = categoryService.findAll()
        }
        if (SecurityUtil.hasAnyPermission(Permission.studentsModify, Permission.hasChildren)) {
            dicts.relativeRoles = studentRelativeRoleRepository.findAll().toList()
        }
        if (SecurityUtil.hasPermission(Permission.manageUsers)) {
            dicts.roles = roleRepository.findAll().map { RoleDto.fromDomainObject(it) }
        }
        if (SecurityUtil.hasAnyPermission(Permission.studentsModify, Permission.teachersRead)) {
            dicts.employees = teacherRepository.findAll().map { DictionaryItemDto.fromDomainObject(it) }
        }
        if (SecurityUtil.hasPermission(Permission.studentsModify)) {
            dicts.promotionSources = promotionSourceRepository.findAll().map { PromotionSourceDto.fromDomainObject(it) }
            dicts.promotionDetails = promotionDetailRepository.findAll().map { PromotionDetailDto.fromDomainObject(it) }
        }
        return dicts
    }
}