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

import com.joins.kidcenter.domain.City
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.CityRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface CityService : FindOneService<City>, SaveService<City, City>, DeleteOneService {
    fun findAll(): SearchResult<City>
}

@Service
@Transactional
open class CityServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: CityRepository
) : EntityService(em, queryBuilder), CityService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): City? = repository.findOne(id)

    @Transactional(readOnly = true)
    override fun findAll(): SearchResult<City> {
        @Suppress("UNCHECKED_CAST")
        val cities = repository.findOrderById()
        return SearchResult(cities, cities.size.toLong())
    }

    override fun save(entity: City): City {
        return repository.save(entity)
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}