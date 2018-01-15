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

package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.StudentCall
import com.joins.kidcenter.domain.StudentCallMethod
import com.joins.kidcenter.domain.StudentCallResult
import com.joins.kidcenter.domain.StudentStatus
import com.joins.kidcenter.utils.DateTimeUtils
import java.time.LocalDateTime

class StudentCallListDto() {
    var id: Long = 0
    var student: EntityRef<Long>? = null
    var relative: EntityNameRef<Long>? = null
    var studentStatus: StudentStatus? = null
    var employee: EntityNameRef<Long>? = null
    var date: LocalDateTime = DateTimeUtils.currentDateTime()
    var method: StudentCallMethod = StudentCallMethod.chat
    var result: StudentCallResult = StudentCallResult.replied
    var comment: String = ""

    companion object {
        fun fromDomainObject(studentCall: StudentCall): StudentCallListDto {
            return StudentCallListDto().apply {
                id = studentCall.id!!
                student = EntityRef(studentCall.student!!.id!!)
                val domainRelative = studentCall.relative
                relative = if (domainRelative != null) EntityNameRef(domainRelative.id!!, domainRelative.name ?: "") else null
                studentStatus = studentCall.student!!.status
                val domainEmployee = studentCall.employee
                employee = if (domainEmployee != null) EntityNameRef(domainEmployee.id!!, domainEmployee.name) else null
                date = studentCall.date
                method = studentCall.method
                result = studentCall.result
                comment = studentCall.comment
            }
        }
    }
}