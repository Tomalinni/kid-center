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

package com.joins.kidcenter.security.config

import com.joins.kidcenter.Config
import com.joins.kidcenter.security.*
import com.joins.kidcenter.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableAutoConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private var unauthorizedHandler: JwtUnauthorizedHandler? = null

    @Autowired
    private var authenticationProvider: JwtAuthenticationProvider? = null

    @Autowired
    private var context: ApplicationContext? = null

    @Bean
    @Throws(Exception::class)
    public override fun authenticationManager(): AuthenticationManager {
        return ProviderManager(listOf(authenticationProvider))
    }

    @Bean
    @Throws(Exception::class)
    open fun authenticationTokenFilterBean(authService: AuthService, tokenHelper: AuthTokenHelper): JwtAuthenticationTokenFilter {
        val authenticationTokenFilter = JwtAuthenticationTokenFilter(authService, tokenHelper)
        authenticationTokenFilter.setAuthenticationManager(authenticationManager())
        authenticationTokenFilter.setAuthenticationSuccessHandler(JwtAuthenticationSuccessHandler())
        return authenticationTokenFilter
    }

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity.csrf().disable() // we don't need CSRF because our token is invulnerable
                .authorizeRequests().antMatchers(*Config.anonymouslyPermittedPaths).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        // Custom JWT based security filter
        httpSecurity.addFilterBefore(context!!.getBean(JwtAuthenticationTokenFilter::class.java), UsernamePasswordAuthenticationFilter::class.java)
        // disable page caching
        httpSecurity.headers().cacheControl().disable()
    }
}