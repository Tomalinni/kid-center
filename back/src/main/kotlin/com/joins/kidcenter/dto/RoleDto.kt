package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.AppRole
import com.joins.kidcenter.security.model.Permission

class RoleDto() {
    var id: String = ""
    var newId: String? = ""
    var permissions: MutableSet<Permission> = mutableSetOf()

    companion object {
        fun fromDomainObject(role: AppRole): RoleDto {
            return RoleDto().apply {
                id = role.id!!
                permissions = role.permissions.map({ it.id!! }).toMutableSet()
            }
        }
    }
}