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

package com.joins.kidcenter.service.persistence

import com.joins.kidcenter.domain.LessonTemplate
import com.joins.kidcenter.dto.DateRange
import com.joins.kidcenter.repository.LessonTemplateRepository

interface OverlappedTemplateEditor {

    fun edit()

    fun overlappedRange(): DateRange
}

abstract class AbstractOverlappedTemplateEditor(val overlappedRange: DateRange) : OverlappedTemplateEditor {

    override fun overlappedRange(): DateRange = overlappedRange
}

class OverlappedStartTemplateEditor(val template: LessonTemplate,
                                    val repository: LessonTemplateRepository,
                                    overlappedRange: DateRange) : AbstractOverlappedTemplateEditor(overlappedRange) {

    override fun edit() {
        template.startDate = overlappedRange.endDate.plusDays(1)
        repository.save(template)
    }
}

class OverlappedEndTemplateEditor(val template: LessonTemplate,
                                  val repository: LessonTemplateRepository,
                                  overlappedRange: DateRange) : AbstractOverlappedTemplateEditor(overlappedRange) {

    override fun edit() {
        template.endDate = overlappedRange.startDate.minusDays(1)
        repository.save(template)
    }
}

class OverlappedFullTemplateEditor(val template: LessonTemplate,
                                   val repository: LessonTemplateRepository,
                                   overlappedRange: DateRange) : AbstractOverlappedTemplateEditor(overlappedRange) {

    override fun edit() {
        repository.delete(template)
    }
}

class OverlappedMiddleTemplateEditor(val template: LessonTemplate,
                                     val repository: LessonTemplateRepository,
                                     overlappedRange: DateRange) : AbstractOverlappedTemplateEditor(overlappedRange) {

    override fun edit() {
        val firstPart = LessonTemplate.copyFrom(template, DateRange(template.startDate, overlappedRange.startDate))
        val secondPart = LessonTemplate.copyFrom(template, DateRange(overlappedRange.endDate, template.endDate))
        repository.delete(template)
        repository.save(firstPart)
        repository.save(secondPart)
    }
}