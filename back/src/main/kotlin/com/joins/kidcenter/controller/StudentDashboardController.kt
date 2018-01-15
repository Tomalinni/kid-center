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
import com.joins.kidcenter.dto.StudentLessonsSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.StudentService
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.sendObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/studentDashboard")
class StudentDashboardController @Autowired constructor(
        val service: StudentService,
        val objectMapper: ObjectMapper) {

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        response.sendObject(objectMapper, service.findDashboardData(id), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}/lessons"), method = arrayOf(RequestMethod.GET))
    fun findLessons(response: HttpServletResponse, @PathVariable id: Long, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        response.sendObject(objectMapper, service.findDashboardLessons(StudentLessonsSearchRequest.Factory.fromMap(id, parameters)))
    }
}
