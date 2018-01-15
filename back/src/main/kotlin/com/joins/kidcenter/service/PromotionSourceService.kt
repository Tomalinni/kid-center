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

import com.joins.kidcenter.domain.PromotionSource
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.PromotionSourceDto
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.PromotionSourceRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface PromotionSourceService : FindOneService<PromotionSourceDto>, SaveService<PromotionSourceDto, PromotionSource>, DeleteOneService {
    fun findAll(): SearchResult<PromotionSourceDto>
}

@Service
@Transactional
open class PromotionSourceServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: PromotionSourceRepository
) : EntityService(em, queryBuilder), PromotionSourceService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): PromotionSourceDto? {
        val promotionSource = repository.findOne(id)
        return if (promotionSource == null) null else PromotionSourceDto.fromDomainObject(promotionSource)
    }

    @Transactional(readOnly = true)
    override fun findAll(): SearchResult<PromotionSourceDto> {
        @Suppress("UNCHECKED_CAST")
        val entities = repository.findAll()
        val results = entities.map { PromotionSourceDto.fromDomainObject(it) }
        return SearchResult(results, results.size.toLong())
    }

    override fun save(entity: PromotionSourceDto): PromotionSource {
        val promotionSource = PromotionSource().apply {
            id = entity.id
            name = entity.name
            hasPromoter = entity.hasPromoter
        }
        return repository.save(promotionSource)
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}