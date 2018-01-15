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
import com.joins.kidcenter.domain.School
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.SchoolService
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.sendDeleteResult
import com.joins.kidcenter.utils.sendObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/schools")
class SchoolController @Autowired constructor(
        val service: SchoolService,
        val objectMapper: ObjectMapper) {


    @RequestMapping("/")
    fun findAll(response: HttpServletResponse) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        response.sendObject(objectMapper, service.findAll())
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        response.sendObject(objectMapper, service.findOne(id))
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody school: School) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendObject(objectMapper, service.save(school))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody school: School) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendObject(objectMapper, service.save(school.apply { this.id = id }))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }
}
