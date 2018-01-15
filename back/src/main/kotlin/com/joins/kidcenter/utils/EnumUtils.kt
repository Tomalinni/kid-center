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

package com.joins.kidcenter.utils

object EnumUtils {

    fun <T : Enum<T>> valueOf(value: String?, options: Array<T>, defaultOption: T): T {
        if (value != null) {
            return options.find { it.name == value } ?: defaultOption
        }
        return defaultOption
    }

    fun <T : Enum<T>> nullableValueOf(value: String?, options: Array<T>): T? {
        if (value != null) {
            return options.find { it.name == value }
        }
        return null
    }

    fun <T : Enum<T>> nullableByOrdinal(ordinal: Int?, options: Array<T>, defaultOption: T?): T? {
        if (ordinal == null || ordinal < 0 || ordinal >= options.size) {
            return defaultOption
        }
        return options[ordinal]
    }

    fun <T : Enum<T>> ordinals(options: Array<T>): List<Int> {
        return options.map { it.ordinal }
    }

    fun <T : Enum<T>> defaultBitmask(options: Array<T>): Int {
        return (2 shl options.size - 1) - 1
    }
}