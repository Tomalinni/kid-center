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

import com.joins.kidcenter.domain.AppPermission
import com.joins.kidcenter.domain.AppRole
import com.joins.kidcenter.domain.AppUser
import com.joins.kidcenter.security.model.Permission
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppUserRepository : CrudRepository<AppUser, String>

@Repository
interface AppRoleRepository : CrudRepository<AppRole, String> {
    @Query(nativeQuery = true, value = "select u.id from appuser u join appuser_approle ur on ur.appuser_id = u.id where ur.roles_id = :roleId")
    fun findUserIdsByRoleId(@Param("roleId") roleId: String): List<String>
}

@Repository
interface AppPermissionRepository : CrudRepository<AppPermission, Permission>
