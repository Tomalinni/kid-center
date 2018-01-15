package com.joins.kidcenter.dto

import com.joins.kidcenter.domain.Homework
import com.joins.kidcenter.domain.LessonAgeGroup
import com.joins.kidcenter.domain.LessonSubject
import com.joins.kidcenter.utils.DateTimeUtils
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

class HomeworkDto() {
    var id: Long = 0L
    var subject: LessonSubject? = LessonSubject.art
    var ageGroup: LessonAgeGroup = LessonAgeGroup.g2_3
    var startDate: LocalDate = DateTimeUtils.currentDate()
    var endDate: LocalDate = DateTimeUtils.currentDate()

    var files: List<String> = listOf()

    companion object {
        fun fromDomainObject(homework: Homework): HomeworkDto {
            return HomeworkDto().apply {
                id = homework.id!!
                subject = homework.subject
                ageGroup = homework.ageGroup
                startDate = homework.startDate
                endDate = homework.endDate
            }
        }
    }
}