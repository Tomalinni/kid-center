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

package com.joins.kidcenter.helper

import com.joins.kidcenter.domain.Gender
import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentRelative
import com.joins.kidcenter.utils.DateTimeUtils
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

interface ThStudentFactory {
    fun student(relatives: Collection<StudentRelative> = listOf()): Student

    fun relative(): StudentRelative
}

@Component
class ThStudentFactoryImpl : ThStudentFactory {

    val studentCounter = AtomicInteger()
    val relativeCounter = AtomicInteger()

    override fun student(relatives: Collection<StudentRelative>): Student {
        val studentIndex = studentCounter.incrementAndGet()
        return Student().apply {
            nameEn = "testEn" + studentIndex
            nameCn = "testCn" + studentIndex
            gender = Gender.boy
            birthDate = DateTimeUtils.currentDate().minusYears(4)
            this.relatives = relatives.toMutableList()
        }
    }

    override fun relative(): StudentRelative {
        val relativeIndex = relativeCounter.incrementAndGet()
        return StudentRelative().apply {
            name = "test" + relativeIndex
            this.role = "testRelativeRole"
        }
    }
}