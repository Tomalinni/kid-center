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

import com.joins.kidcenter.domain.*
import com.joins.kidcenter.domain.StudentSlotStatus.*
import com.joins.kidcenter.repository.StudentCardRepository
import com.joins.kidcenter.repository.StudentRepository
import com.joins.kidcenter.repository.StudentSlotRepository
import com.joins.kidcenter.service.lessons.LessonEvent
import com.joins.kidcenter.service.lessons.LessonEventListener
import com.joins.kidcenter.service.lessons.LessonEventType.*
import com.joins.kidcenter.service.persistence.StudentCodeGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import javax.persistence.EntityManager

interface StudentStatusManager : LessonEventListener {

    fun updateOnCardSell(studentCard: StudentCard)

    fun updateOnCardRemove(studentCard: StudentCard)
}

@Service
@Transactional
open class StudentStatusManagerImpl @Autowired constructor(
        val em: EntityManager,
        val studentRepository: StudentRepository,
        val studentSlotRepository: StudentSlotRepository,
        val studentCardRepository: StudentCardRepository,
        val codeGenerator: StudentCodeGenerator
) : StudentStatusManager {

    override fun onLessonEvent(event: LessonEvent) {
        val studentCard = loadStudentCard(event.studentCardId)
        when (event.type) {
            plan -> updateOnLessonPlan(studentCard)
            unplan, cancel, revoke -> updateOnLessonCancelOrRevoke(studentCard)
            visit -> updateOnLessonVisitOrMiss(studentCard, visited)
            miss -> updateOnLessonVisitOrMiss(studentCard, missed)
        }
    }

    private fun updateOnLessonPlan(studentCard: StudentCard) {
        if (studentCard.visitType == VisitType.trial) {
            val student = studentCard.student!!
            if (student.status == StudentStatus.registered || student.status == StudentStatus.trialEnd) {
                student.status = StudentStatus.lessonPlanned
                studentRepository.save(student)
            }
        }
    }

    private fun updateOnLessonVisitOrMiss(studentCard: StudentCard, targetStatus: StudentSlotStatus) {
        if (studentCard.visitType == VisitType.trial) {
            val student = studentCard.student!!
            if (canHavePlannedLessons(student.status)) {
                if (!hasLessonsInStatus(student.id!!, planned)) {
                    student.status = StudentStatus.trialEnd
                } else if (targetStatus == visited) {
                    student.status = StudentStatus.lessonVisited
                }// else leave current status (lessonPlanned or lessonVisited)

                studentRepository.save(student)
            }
        }
    }

    private fun updateOnLessonCancelOrRevoke(studentCard: StudentCard) {
        if (studentCard.visitType == VisitType.trial) {
            val student = studentCard.student!!
            if (student.status == StudentStatus.lessonPlanned && !hasLessonsInStatus(student.id!!, planned)) {
                if (hasLessonsInStatus(student.id!!, visited)) {
                    student.status = StudentStatus.trialEnd
                } else {
                    student.status = StudentStatus.registered
                }
                studentRepository.save(student)

            } else if (student.status == StudentStatus.lessonVisited && !hasLessonsInStatus(student.id!!, planned)) {
                student.status = StudentStatus.trialEnd
                studentRepository.save(student)
            }
        }
    }

    override fun updateOnCardSell(studentCard: StudentCard) {
        val student = studentCard.student!!
        if (studentCard.visitType.forMembers && student.status != StudentStatus.cardPaid) {
            student.status = StudentStatus.cardPaid
            student.businessId = codeGenerator.nextRegularId()
            studentRepository.save(student)
        }
    }

    override fun updateOnCardRemove(studentCard: StudentCard) {
        if (studentCard.visitType.forMembers) {
            val student = studentCard.student!!
            if (!hasRegularCards(student)) {
                if (hasLessonsInStatus(student.id!!, planned)) {
                    if (hasLessonsInStatus(student.id!!, visited)) {
                        student.status = StudentStatus.lessonVisited
                    } else {
                        student.status = StudentStatus.lessonPlanned
                    }
                } else {
                    if (hasLessonsInStatus(student.id!!, visited)) {
                        student.status = StudentStatus.trialEnd
                    } else {
                        student.status = StudentStatus.registered
                    }
                }
                student.businessId = student.trialBusinessId
                studentRepository.save(student)
            }
        }
    }

    private fun loadStudentCard(studentCardId: Long): StudentCard {
        return studentCardRepository.findOne(studentCardId)
    }

    private fun hasRegularCards(student: Student) = studentCardRepository.findCardsCountByVisitType(student.id!!, VisitType.regular.ordinal) > 0

    private fun canHavePlannedLessons(status: StudentStatus) = arrayOf(StudentStatus.lessonPlanned, StudentStatus.lessonVisited).contains(status)

    private fun hasLessonsInStatus(studentId: Long, status: StudentSlotStatus): Boolean {
        val studentSlotCounts = studentSlotRepository.findStudentSlotsCountGroupedByStatus(studentId).map {
            val studentSlotStatus = StudentSlotStatus.values()[it[0] as Int]
            val slotCount = (it[1] as BigInteger).toLong()
            Pair(studentSlotStatus, slotCount)
        }.toMap()
        return studentSlotCounts[status] != null
    }
}
