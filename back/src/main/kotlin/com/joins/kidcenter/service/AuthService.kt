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

package com.joins.kidcenter.service

import com.joins.kidcenter.Config
import com.joins.kidcenter.controller.AuthRequest
import com.joins.kidcenter.controller.AuthResponse
import com.joins.kidcenter.controller.RegisterRequest
import com.joins.kidcenter.domain.AppRole
import com.joins.kidcenter.domain.AppUser
import com.joins.kidcenter.domain.StudentRelative
import com.joins.kidcenter.dto.EntityId
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.repository.AppUserRepository
import com.joins.kidcenter.repository.StudentRelativeRoleRepository
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.maps.MobileConfirmationService
import com.joins.kidcenter.utils.PasswordEncryptor
import com.joins.kidcenter.utils.ValidatorsUtil
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

interface AuthService {
    fun login(authRequest: AuthRequest): AuthResponse

    fun register(registerRequest: RegisterRequest): SaveResult<AuthResponse>

    fun getUserToken(userId: String): String?

    fun refreshUserTokens(userIds: List<String>): List<String>

    fun invalidateUserTokens(userIds: List<String>)
}

@Service
@Transactional
open class LoginServiceImpl @Autowired constructor(
        val userRepository: AppUserRepository,
        val studentRelativeRoleRepository: StudentRelativeRoleRepository,
        val encryptor: PasswordEncryptor,
        val confirmationService: MobileConfirmationService
) : AuthService {

    @Value("\${jwt.secret}")
    private var secret: String = ""

    private val userTokens: MutableMap<String, ValidatedUserToken> = ConcurrentHashMap()


    override fun login(authRequest: AuthRequest): AuthResponse {
        var token: String? = null
        val login = authRequest.login.trim()
        if (!login.isBlank()) {
            val user = userRepository.findOne(login)
            if (user != null && encryptor.encrypt(authRequest.pass) == user.pass) {
                token = generateUserToken(user)
                userTokens[login] = ValidatedUserToken(token)
            }
        }
        return AuthResponse(token)
    }

    /**
     * Gets token for specified user. Tries to create token if it was not initialized.
     * Returns null if it was invalidated
     */
    override fun getUserToken(userId: String): String? {
        val token = userTokens[userId]
        if (token == null) { //can happen if server was restarted after user had logged in
            val newTokens = refreshUserTokens(listOf(userId))
            if (newTokens.isEmpty()) {
                return null
            }
            return newTokens[0]
        }
        return if (token.valid) token.value else null
    }

    /**
     * Creates tokens for specified users that can be found in database
     */
    override fun refreshUserTokens(userIds: List<String>): List<String> {
        if (!userIds.isEmpty()) {
            val users = userRepository.findAll(userIds).map { Pair(it.id, it) }.toMap()
            val newTokens: MutableList<String> = mutableListOf()

            userIds.forEach { userId ->
                val user = users[userId]
                if (user != null) {
                    val newToken = generateUserToken(user)
                    userTokens[userId] = ValidatedUserToken(newToken)
                    newTokens.add(newToken)
                } else {
                    userTokens.remove(userId)
                }
            }
            return newTokens
        }
        return listOf()
    }

    private fun generateUserToken(user: AppUser): String {
        val mergedPerms: MutableSet<Permission> = getMergedPermissions(user)

        val claims = Jwts.claims().setSubject(user.id)
        claims.put("perms", Permission.toHexString(mergedPerms))

        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact()
    }

    override fun invalidateUserTokens(userIds: List<String>) {
        userIds.forEach {
            val validatedToken = userTokens[it]
            if (validatedToken != null) {
                validatedToken.valid = false
            }
        }
    }

    private fun getMergedPermissions(user: AppUser): MutableSet<Permission> {
        val mergedPerms: MutableSet<Permission> = mutableSetOf()
        user.roles.forEach { role ->
            role.permissions.forEach { perm ->
                if (perm.id != null) {
                    mergedPerms.add(perm.id!!)
                }
            }
        }
        return mergedPerms
    }

    override fun register(registerRequest: RegisterRequest): SaveResult<AuthResponse> {
        val result = ValidatorsUtil.validateRegRelative(registerRequest)
        if (result.hasErrors()) return result

        val existingUser = userRepository.findOne(registerRequest.login)
        if (existingUser != null) {
            result.addValidationMessage(EntityId.regRelatives, "login", "common.validation.login.already.exist")
        }
        if (result.hasErrors()) return result //do not check confirmation to not expire it

        val checkResult = confirmationService.checkConfirmation(registerRequest.confirmationId, registerRequest.mobile, registerRequest.confirmationCode)
        if (checkResult.error != null) {
            result.addValidationMessage(EntityId.regRelatives, "mobile", checkResult.error)
        }
        if (result.hasErrors()) return result

        val user = AppUser().apply {
            id = registerRequest.login
            name = registerRequest.login
            pass = encryptor.encrypt(registerRequest.pass)
            roles = mutableSetOf(AppRole().apply { id = Config.relativeUserRoleId })
            relative = StudentRelative().apply {
                role = studentRelativeRoleRepository.findOne(Config.defaultStudentRelativeRoleId)?.name ?: ""
                name = registerRequest.name
                mobile = registerRequest.mobile
                mobileConfirmed = true
            }
        }
        val savedUser = userRepository.save(user)

        return SaveResult(AuthResponse(generateUserToken(savedUser)))
    }
}

private class ValidatedUserToken(val value: String,
                                 @Volatile var valid: Boolean = true)
