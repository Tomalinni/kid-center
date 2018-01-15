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

package com.joins.kidcenter.dto.internal

import java.util.*

class ParametrizedMessage(
        val text: String,
        val params: Array<out Any> = arrayOf(),
        val category: Any? = null
) {
    constructor(text: String, vararg params: Any) : this(text, params, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ParametrizedMessage

        if (text != other.text) return false
        if (!Arrays.equals(params, other.params)) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + Arrays.hashCode(params)
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "$text, parameters: ${params.joinToString()}"
    }
}