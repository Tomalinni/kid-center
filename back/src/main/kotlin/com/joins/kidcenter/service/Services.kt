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

import com.joins.kidcenter.dto.DeleteResult
import com.joins.kidcenter.dto.SearchResult
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.service.persistence.QuerySourceData
import javax.persistence.EntityManager

interface FindOneService<out T> {
    fun findOne(id: Long): T?
}

interface LoadService<out T> {
    fun load(): T?
}

interface SaveService<in T, out R> {
    fun save(entity: T): R
}

interface DeleteOneService {
    fun delete(id: Long): DeleteResult<Long>
}

abstract class EntityService(
        val em: EntityManager,
        val queryBuilder: EntityListQueryBuilder
) {

    fun <T> findByRequest(searchRequest: TextSearchRequest, entityClass: Class<out Any>, queryColumns: Collection<String>): SearchResult<T> {
        if (searchRequest.isRecordNumbersValid()) {
            val maxResults = searchRequest.pageRecordsCount
            val firstResult = searchRequest.firstRecord - 1
            val sourceData = QuerySourceData(entityClass, queryColumns, searchRequest)
            val (listQuery, countQuery) = queryBuilder.queries(sourceData)

            val results = em.createQuery(listQuery)
                    .setFirstResult(firstResult)
                    .setMaxResults(maxResults)
                    .resultList

            val total = if (results.size == maxResults || firstResult > 0)
                em.createQuery(countQuery).singleResult as Long
            else
                results.size.toLong()

            @Suppress("UNCHECKED_CAST")
            return SearchResult((results as Iterable<T>).toList(), total)
        } else {
            return SearchResult(listOf<T>(), 0)
        }
    }
}