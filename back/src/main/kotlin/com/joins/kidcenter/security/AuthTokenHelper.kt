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

package com.joins.kidcenter.security

import com.joins.kidcenter.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class AuthTokenHelper {

    @Value("\${jwt.header}")
    private val tokenHeader: String? = null

    @Value("\${jwt.param}")
    private val tokenParameter: String? = null

    fun getTokenFromHeader(request: HttpServletRequest): String? {
        val header = request.getHeader(tokenHeader)
        return if (header == null || !header.startsWith(Config.authSchemePrefix)) null else header.substring(Config.authSchemePrefix.length)
    }

    fun getTokenFromParameter(request: HttpServletRequest): String? {
        val param = request.getParameter(tokenParameter)
        return if (param == null) null else param
    }

    fun setTokenToHeader(response: HttpServletResponse, token: String) {
        response.setHeader(tokenHeader, "${Config.authSchemePrefix}$token")
    }
}
