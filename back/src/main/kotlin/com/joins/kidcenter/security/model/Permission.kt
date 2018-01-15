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

package com.joins.kidcenter.security.model

enum class Permission {
    lessonsRead, //View lessons information
    lessonsPlan, //Plan student lessons (suspend)
    lessonsClose, //Mark student presence and absence, close lessons when finished
    lessonsRevoke, //Revoke lessons
    lessonsModify, //Modify lesson templates

    studentsRead, //View student information
    studentsModify, //Create, modify, delete students and related info (student cards, relative, kinder gardens)

    cardsRead, //View card information
    cardsModify, //Create, modify, delete cards

    teachersRead, //View teachers information
    teachersModify, //Create, modify, delete teachers

    paymentsRead, //View payments information
    paymentsModify,

    @Deprecated("False value causes problems with saving student with several relatives that have unconfirmed mobile numbers")
    saveNotConfirmedMobileNumber,
    manageUsers,
    hasChildren,

    homeworkRead, //View homework information
    homeworkModify, //Create, modify, delete homework information

    lessonTemplatesRead, //View lesson template (schedule) information
    lessonTemplatesModify, //Modify lesson template (schedule) information
    studentCardsRead, //View student cards information
    studentCardsModify, //Modify student cards information
    studentCallsRead, //View student calls information
    studentCallsModify,

    studentCardPaymentPrefModify;  //Modify student card payment mapping preferences

    companion object {
        private val chars: CharArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

        fun toHexString(permissions: Collection<Permission>): String {
            val permSize = Permission.values().size
            val bits = BooleanArray(if (permSize % 4 === 0) permSize else (permSize / 4 + 1) * 4)
            val maskChars = CharArray(bits.size / 4)
            permissions.forEach { bits[it.ordinal] = true }
            var i = 0
            while (i < bits.size) {
                var mask = 0
                if (bits[i]) mask += 1
                if (bits[i + 1]) mask += 2
                if (bits[i + 2]) mask += 4
                if (bits[i + 3]) mask += 8
                maskChars[i / 4] = chars[mask]
                i += 4
            }
            return String(maskChars)
        }

        fun fromHexString(hex: String): Set<Permission> {
            val permissions = mutableSetOf<Permission>()
            for (i in 0..hex.length - 1) {
                val mask = Character.digit(hex[i], 16)
                if (mask != -1) {
                    for (j in 0..3) {
                        if ((mask ushr j) % 2 === 1) {
                            val permIndex = i * 4 + j
                            if (permIndex < Permission.values().size) {
                                permissions.add(Permission.values()[permIndex])
                            }
                        }
                    }
                }
            }
            return permissions
        }
    }
}
