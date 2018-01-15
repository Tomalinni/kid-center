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

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.controller.EntityResponse
import com.joins.kidcenter.controller.OperationResponse
import com.joins.kidcenter.domain.Preference
import com.joins.kidcenter.dto.preference.StudentCardPaymentPreference
import com.joins.kidcenter.repository.PreferenceRepository
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.lessons.LessonSlotService
import com.joins.kidcenter.utils.SecurityUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface PreferenceService {
    fun <T> setPreference(id: String, value: String?): OperationResponse

    fun <T> getPreference(id: String): EntityResponse<T>
}

class PreferenceType<T>(val id: String, val clazz: Class<T>, val permission: Permission?)

object PreferenceTypes {
    val studentCardPayment = "studentCardPayment"

    private val types: MutableMap<String, PreferenceType<*>> = mutableMapOf()

    init {
        addType(PreferenceType(studentCardPayment, StudentCardPaymentPreference::class.java, Permission.studentCardPaymentPrefModify))
    }

    private fun addType(type: PreferenceType<*>) {
        types[type.id] = type
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getType(id: String): PreferenceType<T>? {
        return types[id] as PreferenceType<T>?
    }
}

@Service
@Transactional
open class PreferenceServiceImpl @Autowired constructor(
        val repository: PreferenceRepository,
        val mapper: ObjectMapper
) : PreferenceService {


    val log: Logger = LoggerFactory.getLogger(LessonSlotService::class.java)

    override fun <T> setPreference(id: String, value: String?): OperationResponse {
        val type: PreferenceType<T>? = PreferenceTypes.getType(id)
        if (type == null) {
            return OperationResponse.error("common.error.preference.unknown.type")
        } else {
            checkPreferenceTypePermission(type)

            if (value != null) {
                val preferenceObj = preferenceValueFromString(type, value)
                if (preferenceObj == null) {
                    return OperationResponse.error("common.error.preference.corrupted.value")
                } else {
                    setPreference(type, preferenceObj)
                }
            } else {
                repository.delete(id)
            }
        }
        return OperationResponse.success()
    }

    private fun <T> checkPreferenceTypePermission(type: PreferenceType<T>) {
        if (type.permission != null) {
            SecurityUtil.checkPermission(type.permission)
        }
    }

    override fun <T> getPreference(id: String): EntityResponse<T> {
        val type: PreferenceType<T>? = PreferenceTypes.getType(id)
        if (type == null) {
            return EntityResponse(null, OperationResponse.error("common.error.preference.unknown.type"))
        } else {
            val preference = repository.findOne(type.id)
            if (preference != null) {
                val preferenceValue = preferenceValueFromString(type, preference.value)
                if (preferenceValue == null) {
                    return EntityResponse(null, OperationResponse.error("common.error.preference.corrupted.value"))
                } else {
                    return EntityResponse(preferenceValue)
                }
            }
            return EntityResponse(null)
        }
    }

    private fun <T> setPreference(type: PreferenceType<T>, value: T) {
        var preference = repository.findOne(type.id)
        if (preference == null) {
            preference = Preference()
            preference.id = type.id
        }
        preference.value = mapper.writeValueAsString(value)
        repository.save(preference)
    }

    private fun <T> preferenceValueFromString(type: PreferenceType<T>, value: String): T? {
        try {
            return mapper.readValue(value, type.clazz)
        } catch(e: JsonParseException) {
            log.error("Unable to parse preference with id ${type.id} and value $value from stored object. Null will be returned.")
        } catch(e: JsonMappingException) {
            log.error("Unable to map preference with id ${type.id} and value $value from stored object to class ${type.clazz}. Null will be returned.")
        }
        return null
    }
}
