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

package com.joins.kidcenter.repository

import com.joins.kidcenter.domain.AppUser
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface HelperRepository : CrudRepository<AppUser, String> {

    @Query(nativeQuery = true, value = "select count(*) from information_schema.sequences where sequence_schema=:schemaName and sequence_name=:sequenceName")
    fun findSequencesCount(@Param("schemaName") schemaName: String, @Param("sequenceName") sequenceName: String): BigDecimal

    @Query(nativeQuery = true, value = "select sequence_name from information_schema.sequences where sequence_schema=:schemaName")
    fun findSequences(@Param("schemaName") schemaName: String): List<String>
}

