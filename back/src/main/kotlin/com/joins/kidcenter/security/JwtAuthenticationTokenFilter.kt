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
import com.joins.kidcenter.security.exception.JwtTokenMissingException
import com.joins.kidcenter.security.model.JwtAuthenticationToken
import com.joins.kidcenter.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationTokenFilter(val authService: AuthService,
                                   val tokenHelper: AuthTokenHelper) :
        AbstractAuthenticationProcessingFilter(NegatedRequestMatcher(permittedPathsMatcher())) {

    private val log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter::class.java)

    /**
     * Attempt to authenticate request - basically just pass over to another method to authenticate request headers
     */
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        var authToken = tokenHelper.getTokenFromHeader(request)
        if (authToken == null) {
            authToken = tokenHelper.getTokenFromParameter(request)
        }
        log.debug("Auth token retrieved " + authToken)

        if (authToken == null) {
            throw JwtTokenMissingException("No JWT token found in request headers")
        }
        val authRequest = JwtAuthenticationToken(authToken)

        return authenticationManager.authenticate(authRequest)
    }


    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain?, authResult: Authentication) {
        super.successfulAuthentication(request, response, chain, authResult)
        val userToken = authService.getUserToken(authResult.name)
        if (userToken != null) {
            tokenHelper.setTokenToHeader(response, userToken)

            // As this authentication is in HTTP header, after success we need to continue the request normally
            // and return the response as if the resource was not secured at all
            chain!!.doFilter(request, response)
        } else { //user account was deleted some time after login
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        }
    }
}

private fun permittedPathsMatcher(): OrRequestMatcher {
    return OrRequestMatcher(Config.anonymouslyPermittedPaths.map(::AntPathRequestMatcher))
}
