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
import com.joins.kidcenter.domain.StudentCall
import com.joins.kidcenter.dto.StudentCallSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.StudentCallService
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.sendDeleteResult
import com.joins.kidcenter.utils.sendObject
import com.joins.kidcenter.utils.sendSaveResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/studentCalls")
class StudentCallController @Autowired constructor(
        val service: StudentCallService,
        val objectMapper: ObjectMapper) {

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentCallsRead)
        val call: StudentCall? = service.findOne(id)
        response.sendObject(objectMapper, call, StudentController.editFormFilterProvider)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.studentCallsRead)
        val searchResult = service.findAll(StudentCallSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody call: StudentCall) {
        SecurityUtil.checkPermission(Permission.studentCallsModify)
        return response.sendSaveResult(objectMapper, service.save(call), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody call: StudentCall) {
        SecurityUtil.checkPermission(Permission.studentCallsModify)
        call.id = id
        response.sendSaveResult(objectMapper, service.save(call), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentCallsModify)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }
}
