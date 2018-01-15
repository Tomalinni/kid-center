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

package com.joins.kidcenter.service.maps

import com.joins.kidcenter.Config
import com.joins.kidcenter.domain.StudentSlotStatus
import com.joins.kidcenter.repository.StudentSlotRepository
import com.joins.kidcenter.utils.DateTimeUtils
import com.joins.kidcenter.utils.toSqlDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface StudentNextLessonsProvider {
    fun getForStudents(studentIds: Set<Long>): Map<Long, List<String>>
    fun invalidateStudentLessons(studentId: Long)
}

@Service
@Transactional
open class StudentNextLessonsProviderImpl @Autowired constructor(
        private val studentSlotRepository: StudentSlotRepository,
        private val nextLessonsStorage: StudentNextLessonsStorage
) : StudentNextLessonsProvider {

    override fun getForStudents(studentIds: Set<Long>): Map<Long, List<String>> {
        val nextLessons = nextLessonsStorage.getForStudents(studentIds)
        val foundLessons = mutableMapOf<Long, MutableList<String>>()

        val startDate = DateTimeUtils.currentDate().toSqlDate()
        val endDate = DateTimeUtils.currentDate().plusDays(Config.nextLessonsRangeDays).toSqlDate()
        if (nextLessons.notFoundStudentIds.isNotEmpty()) {
            studentSlotRepository.findLessonIdsForStudentsByStatusInRangeOrderedByDateAsc(nextLessons.notFoundStudentIds, startDate, endDate, StudentSlotStatus.planned.ordinal)
                    .forEach {
                        val lessonId = it[0] as String
                        val studentId = (it[1] as BigInteger).toLong()

                        val lessonIds = foundLessons[studentId]
                        if (lessonIds != null) {
                            if (lessonIds.size < Config.nextLessonsLookupCount) {
                                lessonIds.add(lessonId)
                            }
                        } else {
                            foundLessons.put(studentId, mutableListOf(lessonId))
                        }
                        return@forEach
                    }
            nextLessonsStorage.setForStudents(foundLessons)
        }

        foundLessons.putAll(nextLessons.studentIdToLessons)
        return foundLessons
    }

    override fun invalidateStudentLessons(studentId: Long) {
        nextLessonsStorage.invalidateStudentLessons(studentId)
    }
}

@Component
open class StudentNextLessonsStorage {
    private val studentIdToLessons: MutableMap<Long, MutableList<String>> = mutableMapOf()
    private val lock: Lock = ReentrantLock(true)

    fun getForStudents(studentIds: Set<Long>): NextLessons {
        val foundLessons = mutableMapOf<Long, MutableList<String>>()
        val notFoundStudentIds = mutableSetOf<Long>()
        lock.withLock {
            studentIds.forEach { id ->
                val lessonIds = studentIdToLessons[id]
                if (lessonIds != null) {
                    foundLessons[id] = lessonIds
                } else {
                    notFoundStudentIds.add(id)
                }
                return@forEach
            }
        }
        return NextLessons(foundLessons, notFoundStudentIds)
    }

    fun setForStudents(studentIdToLessons: Map<Long, MutableList<String>>) {
        lock.withLock {
            this.studentIdToLessons.putAll(studentIdToLessons)
        }
    }

    fun invalidateStudentLessons(studentId: Long) {
        lock.withLock {
            studentIdToLessons.remove(studentId)
        }
    }

    fun invalidateAllLessons() {
        lock.withLock {
            studentIdToLessons.clear()
        }
    }
}

class NextLessons(val studentIdToLessons: Map<Long, MutableList<String>>,
                  val notFoundStudentIds: Set<Long>)