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

package com.joins.kidcenter.security.util

import com.joins.kidcenter.security.dto.JwtUserDto
import com.joins.kidcenter.security.model.Permission
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
open class JwtTokenParser {

    @Value("\${jwt.secret}")
    private val secret: String? = null

    /**
     * @param token the JWT token to parse
     *
     * @return the User object extracted from specified token or null if a token is invalid.
     */
    fun parseToken(token: String): JwtUserDto? {
        var u: JwtUserDto? = null

        try {
            val body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body
            u = JwtUserDto(body.subject, Permission.fromHexString(body["perms"] as String))

        } catch (e: JwtException) {
            // Simply print the exception and null will be returned for the userDto
            e.printStackTrace()
        }

        return u
    }
}


