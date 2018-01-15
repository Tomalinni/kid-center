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

import com.fasterxml.jackson.annotation.JsonValue
import com.joins.kidcenter.Config
import com.joins.kidcenter.dto.internal.ParametrizedMessage

class SaveResult<T>(
        val obj: T?,
        val validationMessages: ValidationMessages = ValidationMessages()
) {
    constructor(validationMessages: ValidationMessages = ValidationMessages()) : this(null, validationMessages)

    constructor(entity: ValidatedEntity, fieldId: String, message: ParametrizedMessage) :
            this(ValidationMessages(mutableMapOf(
                    Pair(entity, EntityValidationMessages(mutableMapOf(
                            Pair(fieldId, message)
                    )))
            )))

    constructor(entityId: EntityId, message: String) : this(ValidatedEntity(entityId), Config.commonValidationMessage, ParametrizedMessage(message))

    /**
     * Adds text message to current validationMessages.
     */
    fun addValidationMessage(id: EntityId, fieldId: String, message: String?) {
        if (message != null) {
            addValidationMessage(id, fieldId, ParametrizedMessage(message))
        }
    }

    /**
     * Adds parametrized message to current validationMessages.
     * {@code message} parametrized message.
     */
    fun addValidationMessage(id: EntityId, fieldId: String, message: ParametrizedMessage?) {
        if (message != null) {
            addValidationMessage(id, null, fieldId, message)
        }
    }

    /**
     * Adds parametrized message to current validationMessages for entity specified by index.
     * {@code message} parametrized message.
     */
    fun addValidationMessage(id: EntityId, index: Int?, fieldId: String, message: ParametrizedMessage?) {
        if (message != null) {
            validationMessages.getOrInit(ValidatedEntity(id, index)).put(fieldId, message)
        }
    }

    fun success() = validationMessages.isEmpty()

    fun hasErrors(): Boolean {
        return !validationMessages.isEmpty()
    }
}

data class ValidatedEntity(val entityId: EntityId,
                           val index: Int?) {
    constructor(entityId: EntityId) : this(entityId, null)

    @JsonValue
    fun toJson() = if (index == null) entityId.toString() else "$entityId-$index"
}

class ValidationMessages(
        val messages: MutableMap<ValidatedEntity, EntityValidationMessages> = mutableMapOf()
) {
    fun getOrInit(entity: ValidatedEntity): EntityValidationMessages {
        var entityMessages = messages[entity]
        if (entityMessages == null) {
            entityMessages = EntityValidationMessages()
            messages[entity] = entityMessages
        }
        return entityMessages
    }

    fun isEmpty() = messages.isEmpty()

    @JsonValue
    fun toJson() = messages
}

class EntityValidationMessages(
        val messages: MutableMap<String, ParametrizedMessage> = mutableMapOf()
) {
    fun getOrInit(id: String) = messages[id]

    fun put(fieldId: String, message: ParametrizedMessage) {
        messages[fieldId] = message
    }

    @JsonValue
    fun toJson() = messages
}

enum class EntityId {
    students, relatives, studentCards, studentCardPayment, studentCalls, cards, teachers, lessonTemplates, payments, cities, schools, accounts, categories, regRelatives, profile, users, roles, homework
}