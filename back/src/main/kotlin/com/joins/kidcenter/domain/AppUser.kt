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

package com.joins.kidcenter.domain

import com.joins.kidcenter.security.model.Permission
import javax.persistence.*

@Entity
class AppUser() {
    @Id
    var id: String? = null
    var name: String = ""
    var pass: String = ""

    @ManyToMany(fetch = FetchType.EAGER)
    var roles: MutableSet<AppRole> = mutableSetOf()

    @ManyToOne(cascade = arrayOf(CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH))
    @JoinColumn(name = "relative_id",
            foreignKey = ForeignKey(name = "appuser_relative_id_fkey"))
    var relative: StudentRelative? = null
}


@Entity
class AppRole() {
    @Id
    var id: String? = null

    @ManyToMany(fetch = FetchType.EAGER)
    var permissions: MutableSet<AppPermission> = mutableSetOf()
}

@Entity
class AppPermission() {
    @Id
    @Enumerated
    var id: Permission? = null
}
