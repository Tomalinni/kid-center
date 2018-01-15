package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.dto.UserDto
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.UserService
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/users")
class UserController @Autowired constructor(
        val service: UserService,
        val encryptor: PasswordEncryptor,
        val objectMapper: ObjectMapper) {

    @RequestMapping("/")
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        val searchResult = service.findAll(TextSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: String) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        val user: UserDto? = service.findOne(id)
        if (user != null) {
            response.sendObject(objectMapper, user)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody user: UserDto) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        if (!user.pass.isNullOrBlank()) {
            user.pass = encryptor.encrypt(user.pass)
        }
        response.sendSaveResult(objectMapper, service.save(user))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: String, @RequestBody user: UserDto) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        if (user.passChanged && !user.pass.isNullOrBlank()) {
            user.pass = encryptor.encrypt(user.pass)
        }
        response.sendSaveResult(objectMapper, service.save(user.apply { this.id = id }))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: String) {
        SecurityUtil.checkPermission(Permission.manageUsers)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }
}
