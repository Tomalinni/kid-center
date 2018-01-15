package com.joins.kidcenter.service.mail

import com.joins.kidcenter.domain.EmailNotification
import com.joins.kidcenter.domain.LessonSlot
import com.joins.kidcenter.dto.HomeworkDto
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

interface AppMailService {

    fun sendHomework(lesson: LessonSlot, homework: HomeworkDto?)
}

@Service
open class AppMailServiceImpl @Autowired constructor(
        val freemarker: Configuration,
        val mailSender: AppMailSender,
        val storageService: FileStorageServiceImpl
) : AppMailService {

    val log = LoggerFactory.getLogger(AppMailServiceImpl::class.java)!!

    override fun sendHomework(lesson: LessonSlot, homework: HomeworkDto?) {
        val messages: MutableList<AppMimeMessage> = mutableListOf()
        if (homework == null) {
            log.warn("Nothing to send for lesson with ID ${lesson.id}")
            return
        }
        val files = storageService.providers().homework(homework.id).list()
        if (files.isEmpty()) {
            log.warn("Nothing to send for lesson with ID ${lesson.id} and homework ID ${homework.id}")
            return
        }

        lesson.students.filter { it.student != null }.forEach {
            val student = it.student!!
            student.relatives.forEach {
                val message = getTemplateContent("mail/fm_homeworkTemplate.ftl", mapOf("lesson" to lesson, "student" to student, "relative" to it))
                if (it.emailNotifications.contains(EmailNotification.homework) && !it.mail.isNullOrEmpty()) {
                    messages.add(AppMimeMessage("Homework", message, files, listOf(it.mail!!)))
                }
            }
        }
        if (messages.isEmpty()) {
            log.warn("No recipients for lesson with ID ${lesson.id} and homework ID ${homework.id}.")
        } else {
            mailSender.sendMails(*messages.toTypedArray())
        }
    }

    fun getTemplateContent(template: String, model: Map<String, Any>): String {
        val content = StringBuffer()
        try {
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarker.getTemplate(template), model))
            return content.toString()
        } catch (e: Exception) {
            log.error("Exception occurred while processing template:" + e.message)
        }
        return ""
    }

}