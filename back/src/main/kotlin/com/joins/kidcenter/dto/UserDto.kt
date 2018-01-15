package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.AppUser

class UserDto() {
    var id: String = ""
    var newId: String? = ""
    var name: String = ""
    var pass: String = ""
    var passChanged: Boolean = false
    var roles: List<RoleDto> = mutableListOf()

    companion object {
        fun fromDomainObject(user: AppUser): UserDto {
            return UserDto().apply {
                id = user.id!!
                name = user.name
                roles = user.roles.map { RoleDto.fromDomainObject(it) }
            }
        }
    }
}