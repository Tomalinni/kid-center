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

package com.joins.kidcenter.utils

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

object PredicateUtil {
    private val defaultMinTokenLength = 2

    fun <T> fieldsContainClause(from: Root<T>, text: String?, fields: Collection<String>, cb: CriteriaBuilder, minTokenLength: Int = defaultMinTokenLength): Predicate {
        if (text == null || text.isBlank()) return cb.trueClause()

        val predicates: MutableCollection<Predicate> = mutableListOf()
        val tokens = text.toLowerCase().split(Regex("\\s+")).filter { it.length >= minTokenLength }
        for (token in tokens) {
            for (field in fields) {
                predicates.add(cb.ilike(from.get(field), token))
            }
        }
        return cb.or(*predicates.toTypedArray())
    }

}
