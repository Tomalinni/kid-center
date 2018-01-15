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

package com.joins.kidcenter.domain

import com.joins.kidcenter.utils.DateTimeUtils
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "student_call")
class StudentCall : Persistable {
    @Id
    @SequenceGenerator(name = "student_call_gen", sequenceName = "student_call_seq", allocationSize = 1)
    @GeneratedValue(generator = "student_call_gen")
    override var id: Long? = null

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "student_id", foreignKey = ForeignKey(name = "fk_student_call_student"))
    var student: Student? = null

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "relative_id", foreignKey = ForeignKey(name = "fk_student_call_relative"))
    var relative: StudentRelative? = null

    @ManyToOne(cascade = arrayOf())
    @JoinColumn(name = "employee_id", foreignKey = ForeignKey(name = "fk_student_call_employee"))
    var employee: Teacher? = null
    var date: LocalDateTime = DateTimeUtils.currentDateTime()
    var method: StudentCallMethod = StudentCallMethod.chat
    var result: StudentCallResult = StudentCallResult.replied
    var comment: String = ""

}

enum class StudentCallMethod(val results: Array<StudentCallResult>) {
    chat(arrayOf(StudentCallResult.replied, StudentCallResult.ignored, StudentCallResult.blocked)),
    phone(arrayOf(StudentCallResult.replied, StudentCallResult.notResponded, StudentCallResult.aborted, StudentCallResult.turnedOff, StudentCallResult.notExist, StudentCallResult.invalidNumber)),
    reception(arrayOf(StudentCallResult.replied)),
    mail(arrayOf(StudentCallResult.replied, StudentCallResult.ignored))
}

enum class StudentCallResult {
    replied, ignored, blocked, notResponded, aborted, turnedOff, notExist, invalidNumber
}

enum class StudentImpression {
    approvement, denial, doubt, strange, spy, payPromise, notDefined, play
}
