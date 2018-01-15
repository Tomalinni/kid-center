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

import com.joins.kidcenter.domain.Teacher
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.repository.TeacherRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.ValidatorsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface TeacherService : FindOneService<Teacher>, SaveService<Teacher, SaveResult<Teacher>>, DeleteOneService {
    fun findAll(searchRequest: TextSearchRequest): SearchResult<Teacher>
}

@Service
@Transactional
open class TeacherServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: TeacherRepository
) : EntityService(em, queryBuilder), TeacherService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): Teacher? = repository.findOne(id)

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: TextSearchRequest): SearchResult<Teacher> {
        return findByRequest(searchRequest, Teacher::class.java, listOf("name"))
    }

    override fun save(entity: Teacher): SaveResult<Teacher> {
        val result: SaveResult<Teacher> = ValidatorsUtil.validateTeacher(entity)
        if (result.hasErrors()) {
            return result
        }
        return SaveResult(repository.save(entity))
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}