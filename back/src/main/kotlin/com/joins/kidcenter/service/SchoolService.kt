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

import com.joins.kidcenter.domain.School
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.SchoolRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface SchoolService : FindOneService<School>, SaveService<School, School>, DeleteOneService {
    fun findAll(): SearchResult<School>
}

@Service
@Transactional
open class SchoolServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: SchoolRepository
) : EntityService(em, queryBuilder), SchoolService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): School? = repository.findOne(id)

    @Transactional(readOnly = true)
    override fun findAll(): SearchResult<School> {
        @Suppress("UNCHECKED_CAST")
        val cities = repository.findOrderById()
        return SearchResult(cities, cities.size.toLong())
    }

    override fun save(entity: School): School {
        return repository.save(entity)
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}