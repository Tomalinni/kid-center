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

package com.joins.kidcenter.controller.aop

import com.joins.kidcenter.controller.OperationResponse
import com.joins.kidcenter.controller.StudentController
import com.joins.kidcenter.service.exceptions.OperationException
import org.hibernate.exception.ConstraintViolationException
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.naming.AuthenticationException
import javax.servlet.http.HttpServletRequest

@ControllerAdvice(basePackageClasses = arrayOf(StudentController::class))
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseBody
    internal fun handleControllerException(request: HttpServletRequest, ex: Throwable): ResponseEntity<*> {
        val cause = ex.cause
        if (cause is ConstraintViolationException) {
            val sqlException = cause.sqlException as PSQLException
            if (sqlException.sqlState == "23503") {
                return ResponseEntity(OperationResponse.error("common.table.item.not.deleted", sqlException.serverErrorMessage.table), HttpStatus.CONFLICT)
            }
        }

        if (ex is AuthenticationException || ex is UsernameNotFoundException) {
            return ResponseEntity<Any>(OperationResponse.error("common.auth.unauthorized"), HttpStatus.UNAUTHORIZED)
        }
        if (ex is AccessDeniedException) {
            return ResponseEntity<Any>(OperationResponse.error("common.auth.forbidden"), HttpStatus.FORBIDDEN)
        }
        if (ex is IllegalArgumentException) {
            return ResponseEntity<Any>(OperationResponse.error("common.bad.request", arrayOf(ex.message ?: "")), HttpStatus.BAD_REQUEST)
        }
        if (ex is OperationException) {
            return ResponseEntity(ex.result, ex.status)
        }

        log.error("Handled unspecified exception in global handler", ex)
        return ResponseEntity(OperationResponse.error(ex.message ?: ""), HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
