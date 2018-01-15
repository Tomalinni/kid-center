package com.joins.kidcenter.service.mail

import java.io.File

interface AppMailSender {

    fun sendMails(vararg messages: AppMimeMessage)
}

data class AppMimeMessage(val subject: String, val text: String, val attachments: List<File>, val to: List<String>)