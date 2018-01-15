package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.domain.StudentRelativeRole
import com.joins.kidcenter.dto.EntityRef
import com.joins.kidcenter.repository.StudentRelativeRoleRepository
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.utils.SecurityUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("data/studentRelativeRoles")
class StudentRelativeRoleController @Autowired constructor(
        val repository: StudentRelativeRoleRepository,
        val objectMapper: ObjectMapper) {

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(@RequestBody role: StudentRelativeRole): EntityRef<Long> { //todo create roles on UI
        SecurityUtil.checkPermission(Permission.studentsModify)
        val savedRole = repository.save(role)
        return EntityRef(savedRole.id!!)
    }
}
