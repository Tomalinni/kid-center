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

import com.joins.kidcenter.domain.StudentRelative
import com.joins.kidcenter.dto.RelativeChildrenDto
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.repository.AppUserRepository
import com.joins.kidcenter.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RelativeChildrenService : LoadService<RelativeChildrenDto>, SaveService<RelativeChildrenDto, SaveResult<RelativeChildrenDto>>

@Service
@Transactional
open class RelativeChildrenServiceImpl @Autowired constructor(
        val userRepository: AppUserRepository
) : RelativeChildrenService {
    override fun load(): RelativeChildrenDto? {
        val user = userRepository.findOne(SecurityUtil.subject())
        if (user != null && user.relative != null) {
            return RelativeChildrenDto.fromDomainObject(user.relative!!)
        }
        return null
    }

    override fun save(entity: RelativeChildrenDto): SaveResult<RelativeChildrenDto> {
        val user = userRepository.findOne(SecurityUtil.subject())
        if (user != null) {
            if (user.relative == null) {
                user.relative = StudentRelative()
            }
            user.relative!!.apply {
                role = entity.role
                //todo save students
            }
        }
        return SaveResult(RelativeChildrenDto.fromDomainObject(user.relative!!))
    }
}