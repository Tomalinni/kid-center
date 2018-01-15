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

import org.apache.commons.lang3.StringUtils

object EscapeUtil {

    val defaultEscapeString = "\\"

    private val likeWildcards = arrayOf("%", "_")
    private val defaultEscapedLikeWildcards = getReplacements(likeWildcards, defaultEscapeString)

    fun escapeLike(param: String, escapeString: String = defaultEscapeString): String {
        val replacements = if (escapeString == defaultEscapeString) defaultEscapedLikeWildcards else getReplacements(likeWildcards, escapeString)
        return StringUtils.replaceEach(param, likeWildcards, replacements)
    }

    private fun getReplacements(searchArr: Array<String>, escapeString: String) =
            searchArr.map({ "$escapeString$it" }).toTypedArray()

}
