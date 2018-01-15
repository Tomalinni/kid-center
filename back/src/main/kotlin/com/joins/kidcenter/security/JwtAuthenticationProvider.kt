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

import com.joins.kidcenter.security.exception.JwtTokenMalformedException
import com.joins.kidcenter.security.model.AuthenticatedUser
import com.joins.kidcenter.security.model.JwtAuthenticationToken
import com.joins.kidcenter.security.util.JwtTokenParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
open class JwtAuthenticationProvider @Autowired constructor(
        val jwtTokenParser: JwtTokenParser
) : AbstractUserDetailsAuthenticationProvider() {

    override fun supports(authentication: Class<*>): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

    @Throws(AuthenticationException::class)
    override fun additionalAuthenticationChecks(userDetails: UserDetails, authentication: UsernamePasswordAuthenticationToken) {
    }

    @Throws(AuthenticationException::class)
    override fun retrieveUser(username: String, authentication: UsernamePasswordAuthenticationToken): UserDetails {
        val jwtAuthenticationToken = authentication as JwtAuthenticationToken
        val token = jwtAuthenticationToken.token

        val parsedUser = jwtTokenParser.parseToken(token) ?: throw JwtTokenMalformedException("JWT token is not valid")

        return AuthenticatedUser(parsedUser)
    }

}
