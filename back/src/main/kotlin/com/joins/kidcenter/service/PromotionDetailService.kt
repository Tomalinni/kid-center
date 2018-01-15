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

import com.joins.kidcenter.domain.PromotionDetail
import com.joins.kidcenter.domain.PromotionSource
import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.PromotionDetailDto
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.repository.PromotionDetailRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface PromotionDetailService : FindOneService<PromotionDetailDto>, SaveService<PromotionDetailDto, PromotionDetailDto>, DeleteOneService {
    fun findAll(): SearchResult<PromotionDetailDto>
}

@Service
@Transactional
open class PromotionDetailServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: PromotionDetailRepository
) : EntityService(em, queryBuilder), PromotionDetailService {

    @Transactional(readOnly = true)
    override fun findOne(id: Long): PromotionDetailDto? {
        val promotionDetail = repository.findOne(id)
        return if (promotionDetail == null) null else PromotionDetailDto.fromDomainObject(promotionDetail)
    }

    @Transactional(readOnly = true)
    override fun findAll(): SearchResult<PromotionDetailDto> {
        @Suppress("UNCHECKED_CAST")
        val entities = repository.findAll()
        val results = entities.map { PromotionDetailDto.fromDomainObject(it) }
        return SearchResult(results, results.size.toLong())
    }

    override fun save(entity: PromotionDetailDto): PromotionDetailDto {
        val promotionDetail = PromotionDetail().apply {
            id = entity.id
            name = entity.name
            promotionSource = PromotionSource().apply { id = entity.promotionSource!!.id }
        }
        return PromotionDetailDto.fromDomainObject(repository.save(promotionDetail))
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}