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
import com.joins.kidcenter.service.PreferenceService
import com.joins.kidcenter.utils.sendObject
import com.joins.kidcenter.utils.sendStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/preferences")
class PreferenceController @Autowired constructor(
        val service: PreferenceService,
        val objectMapper: ObjectMapper) {

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: String) {
        val preference: EntityResponse<Any> = service.getPreference(id)
        if (preference.success()) {
            response.sendObject(objectMapper, preference.obj)
        } else {
            response.sendStatus(HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: String, @RequestBody preferenceValue: String) {
        response.sendObject(objectMapper, service.setPreference<Any>(id, preferenceValue))
    }
}
