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

import com.joins.kidcenter.CommonConfiguration
import com.joins.kidcenter.IntgTestConfiguration
import com.joins.kidcenter.db.DbSequenceHelper
import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentRelative
import com.joins.kidcenter.helper.ThFactories
import com.joins.kidcenter.helper.assert.StudentAssert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(CommonConfiguration::class, IntgTestConfiguration::class))
@ActiveProfiles(profiles = arrayOf("test"))
open class StudentSiblingsTest {
    @Autowired
    var studentService: StudentService? = null

    @Autowired
    var factories: ThFactories? = null
    @Autowired
    var dbHelper: DbSequenceHelper? = null

    @Before
    fun setUp() {
        dbHelper!!.createRequiredSequences()
        dbHelper!!.clearTables()
        dbHelper!!.resetCreatedSequences()
    }

    @Test
    fun testAddSiblingsWithMergedRelatives() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        studentService!!.findOne(student1.id!!)
        val savedStudent1 = studentService!!.findOne(student1.id!!)
        StudentAssert(savedStudent1!!)
                .relativeIds(listOf(relative1.id!!, relative2.id!!))
                .siblingIds(listOf(student2.id!!))

        val savedStudent2 = studentService!!.findOne(student2.id!!)
        StudentAssert(savedStudent2!!)
                .relativeIds(listOf(relative1.id!!, relative2.id!!))
                .siblingIds(listOf(student1.id!!))
    }

    @Test
    fun testAddSiblingsToNewStudent() {
        val student1: Student = factories!!.student.student(listOf(factories!!.student.relative()))
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        studentService!!.findOne(student1.id!!)
        val savedStudent1 = studentService!!.findOne(student1.id!!)
        StudentAssert(savedStudent1!!)
                .relativeIds(listOf(relative1.id!!, relative2.id!!))
                .siblingIds(listOf(student2.id!!))

        val savedStudent2 = studentService!!.findOne(student2.id!!)
        StudentAssert(savedStudent2!!)
                .relativeIds(listOf(relative1.id!!, relative2.id!!))
                .siblingIds(listOf(student1.id!!))
    }

    @Test
    fun testAddSiblingsWithChangedRelatives() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative3 = factories!!.student.relative().apply { name = "newRelative" }

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative3)
        studentService!!.save(student1)

        val savedStudent1 = studentService!!.findOne(student1.id!!)
        val relative3Id = savedStudent1!!.relatives.find { it.name == "newRelative" }!!.id!!

        StudentAssert(savedStudent1)
                .relativeIds(listOf(relative1.id!!, relative3Id))
                .siblingIds(listOf(student2.id!!))

        val savedStudent2 = studentService!!.findOne(student2.id!!)
        StudentAssert(savedStudent2!!)
                .relativeIds(listOf(relative1.id!!, relative3Id))
                .siblingIds(listOf(student1.id!!))
    }

    @Test
    fun testAddSiblingsWithMergeFamilies() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        val student3: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative3: StudentRelative = student3.relatives[0]

        val student4: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative4: StudentRelative = student4.relatives[0]

        student3.siblings = listOf(student4)
        student3.relatives = mutableListOf(relative3, relative4)
        studentService!!.save(student3)

        student1.siblings = listOf(student2, student3)
        student1.relatives = mutableListOf(relative1, relative2, relative3, relative4)
        studentService!!.save(student1)

        val commonRelativeIds = listOf(relative1.id!!, relative2.id!!, relative3.id!!, relative4.id!!)
        val fullSiblingIds = listOf(student1.id!!, student2.id!!, student3.id!!, student4.id!!)
        val commonFamilyId = student1.family!!.id!!

        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student1.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student2.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student3.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student3.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student4.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student4.id!!))
                .familyId(commonFamilyId)
    }

    @Test
    fun testAddSiblingToFamily() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        val student3: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative3: StudentRelative = student3.relatives[0]

        //add student3 to existing family of student1
        student1.siblings = listOf(student2, student3)
        student1.relatives = mutableListOf(relative1, relative2, relative3)
        studentService!!.save(student1)

        val commonRelativeIds = listOf(relative1.id!!, relative2.id!!, relative3.id!!)
        val fullSiblingIds = listOf(student1.id!!, student2.id!!, student3.id!!)
        val commonFamilyId = student1.family!!.id!!

        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student1.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student2.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student3.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student3.id!!))
                .familyId(commonFamilyId)
    }

    @Test
    fun testAddFamilyToSibling() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        val student3: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative3: StudentRelative = student3.relatives[0]

        //add student1 and his family to student3
        student3.siblings = listOf(student1)
        student3.relatives = mutableListOf(relative1, relative2, relative3)
        studentService!!.save(student3)

        val commonRelativeIds = listOf(relative1.id!!, relative2.id!!, relative3.id!!)
        val fullSiblingIds = listOf(student1.id!!, student2.id!!, student3.id!!)
        val commonFamilyId = student1.family!!.id!!

        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student1.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student2.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student3.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student3.id!!))
                .familyId(commonFamilyId)
    }

    @Test
    fun testAddFamilyToNewSibling() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2)
        studentService!!.save(student1)

        val student3: Student = factories!!.student.student(listOf(factories!!.student.relative()))
        val relative3: StudentRelative = student3.relatives[0]

        //add student1 and his family to student3
        student3.siblings = listOf(student1)
        student3.relatives = mutableListOf(relative1, relative2, relative3)
        studentService!!.save(student3)

        val commonRelativeIds = listOf(relative1.id!!, relative2.id!!, relative3.id!!)
        val fullSiblingIds = listOf(student1.id!!, student2.id!!, student3.id!!)
        val commonFamilyId = student1.family!!.id!!

        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student1.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student2.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student3.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student3.id!!))
                .familyId(commonFamilyId)
    }

    @Test
    fun testRemoveSiblings() {
        val student1: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative1: StudentRelative = student1.relatives[0]

        val student2: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative2: StudentRelative = student2.relatives[0]

        val student3: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative3: StudentRelative = student3.relatives[0]

        val student4: Student = studentService!!.save(factories!!.student.student(listOf(
                factories!!.student.relative()
        ))).obj!!
        val relative4: StudentRelative = student4.relatives[0]

        student1.siblings = listOf(student2, student3, student4)
        student1.relatives = mutableListOf(relative1, relative2, relative3, relative4)
        studentService!!.save(student1)

        val commonRelativeIds = listOf(relative1.id!!, relative2.id!!, relative3.id!!, relative4.id!!)
        val fullSiblingIds = listOf(student1.id!!, student2.id!!, student3.id!!, student4.id!!)
        val commonFamilyId = student1.family!!.id!!

        StudentAssert(studentService!!.findOne(student1.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(fullSiblingIds.minus(student1.id!!))
                .familyId(commonFamilyId)

        //remove

        student1.siblings = listOf(student2)
        student1.relatives = mutableListOf(relative1, relative2, relative3, relative4)
        studentService!!.save(student1)

        val savedStudent1 = studentService!!.findOne(student1.id!!)!!
        val student1RelativeIds = savedStudent1.relatives.map { it.id!! }
        StudentAssert(savedStudent1)
                .relativeIds(student1RelativeIds)
                .siblingIds(listOf(student2.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student2.id!!)!!)
                .relativeIds(student1RelativeIds)
                .siblingIds(listOf(student1.id!!))
                .familyId(commonFamilyId)

        StudentAssert(studentService!!.findOne(student3.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(listOf())
                .noFamily()

        StudentAssert(studentService!!.findOne(student4.id!!)!!)
                .relativeIds(commonRelativeIds)
                .siblingIds(listOf())
                .noFamily()
    }
}