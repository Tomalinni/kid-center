package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.dto.RoleDto
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.repository.AppRoleRepository
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.RoleService
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/roles")
class RoleController @Autowired constructor(
        val service: RoleService,
        val objectMapper: ObjectMapper,
        val repository: AppRoleRepository) {

    @RequestMapping("/")
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        val searchResult = service.findAll(TextSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: String) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        val role: RoleDto? = service.findOne(id)
        if (role != null) {
            response.sendObject(objectMapper, role)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody role: RoleDto) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        response.sendSaveResult(objectMapper, service.save(role))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: String, @RequestBody role: RoleDto) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        response.sendSaveResult(objectMapper, service.save(role.apply { this.id = id }))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: String) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }
}