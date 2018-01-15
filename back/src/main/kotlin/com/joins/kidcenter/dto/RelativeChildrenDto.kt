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

import com.joins.kidcenter.domain.Gender
import com.joins.kidcenter.domain.KinderGarden
import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentRelative
import java.time.LocalDate

class RelativeChildrenDto() {
    var id: Long = 0
    var role: String = ""

    var students: Collection<RelativeChildDto> = emptyList()

    companion object {
        fun fromDomainObject(relative: StudentRelative): RelativeChildrenDto {
            return RelativeChildrenDto().apply {
                id = relative.id!!
                role = relative.role
                students = relative.students.map { RelativeChildDto.fromDomainObject(it) }.toList()
            }
        }
    }
}

class RelativeChildDto() {
    var id: Long = 0
    var nameEn: String? = ""
    var nameCn: String? = ""
    var birthDate: LocalDate = Student.defaultStudentBirthDate
    var gender: Gender = Gender.boy

    var kinderGarden: KinderGarden? = null


    companion object {
        fun fromDomainObject(student: Student): RelativeChildDto {
            return RelativeChildDto().apply {
                id = student.id!!
                nameCn = student.nameCn
                nameEn = student.nameEn
                birthDate = student.birthDate
                gender = student.gender
                kinderGarden = student.kinderGarden
            }
        }
    }
}
