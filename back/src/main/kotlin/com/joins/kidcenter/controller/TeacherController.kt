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

package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.domain.Teacher
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.TeacherService
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/teachers")
class TeacherController @Autowired constructor(
        val service: TeacherService,
        val objectMapper: ObjectMapper) {


    @RequestMapping("/")
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.teachersRead)
        val searchResult = service.findAll(TextSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.teachersRead)
        val teacher: Teacher? = service.findOne(id)
        if (teacher != null) {
            response.sendObject(objectMapper, teacher)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody teacher: Teacher) {
        SecurityUtil.checkPermission(Permission.teachersModify)
        response.sendSaveResult(objectMapper, service.save(teacher))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody teacher: Teacher) {
        SecurityUtil.checkPermission(Permission.teachersModify)
        teacher.id = id
        val modifiedTeacher = service.save(teacher)
        response.sendSaveResult(objectMapper, modifiedTeacher)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.teachersModify)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }

}
