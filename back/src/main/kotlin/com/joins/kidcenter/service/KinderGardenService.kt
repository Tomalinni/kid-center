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

import com.joins.kidcenter.domain.KinderGarden
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.repository.KinderGardenRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface KinderGardenService : SaveService<KinderGarden, KinderGarden> {
    fun findAll(searchRequest: TextSearchRequest): SearchResult<KinderGarden>
}

@Service
@Transactional
open class KinderGardenServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: KinderGardenRepository
) : EntityService(em, queryBuilder), KinderGardenService {

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: TextSearchRequest): SearchResult<KinderGarden> {
        return findByRequest(searchRequest, KinderGarden::class.java, listOf("name", "address", "phone"))
    }

    override fun save(entity: KinderGarden): KinderGarden {
        return repository.save(entity)
    }
}