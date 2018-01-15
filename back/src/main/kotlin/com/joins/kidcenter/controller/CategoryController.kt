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
import com.joins.kidcenter.dto.CategoryDto
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.CategoryService
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.sendDeleteResult
import com.joins.kidcenter.utils.sendObject
import com.joins.kidcenter.utils.sendSaveResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/categories")
class CategoryController @Autowired constructor(
        val service: CategoryService,
        val objectMapper: ObjectMapper) {


    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        response.sendObject(objectMapper, service.findAll())
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        response.sendObject(objectMapper, service.findOne(id))
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody category: CategoryDto) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendSaveResult(objectMapper, service.save(category))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody category: CategoryDto) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendSaveResult(objectMapper, service.save(category.apply { this.id = id }))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }
}
