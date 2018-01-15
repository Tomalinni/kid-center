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
import com.joins.kidcenter.service.AuthService
import com.joins.kidcenter.utils.sendObject
import com.joins.kidcenter.utils.sendSaveResult
import com.joins.kidcenter.utils.sendStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/auth")
class AuthController @Autowired constructor(
        val authService: AuthService,
        val objectMapper: ObjectMapper) {

    @RequestMapping("/login", method = arrayOf(RequestMethod.POST))
    fun login(response: HttpServletResponse, @RequestBody request: AuthRequest) {
        val authResponse = authService.login(request)
        if (authResponse.token != null) {
            response.sendObject(objectMapper, authResponse)
        } else {
            response.sendStatus(HttpStatus.FORBIDDEN)
        }
    }

    @RequestMapping("/register", method = arrayOf(RequestMethod.POST))
    fun register(response: HttpServletResponse, @RequestBody request: RegisterRequest) {
        response.sendSaveResult(objectMapper, authService.register(request))
    }
}

class AuthRequest {
    var login: String = ""
    var pass: String = ""
}

class AuthResponse(
        val token: String?
)

class RegisterRequest {
    var login: String = ""
    var name: String = ""
    var mobile: String = ""
    var confirmationId: String = ""
    var confirmationCode: String = ""
    var pass: String = ""
    var passRepeat: String = ""
}
