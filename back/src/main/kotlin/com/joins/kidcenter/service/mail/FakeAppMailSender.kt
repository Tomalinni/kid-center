package com.joins.kidcenter.service.mail

import org.slf4j.LoggerFactory

class FakeAppMailSender() : AppMailSender {

    override fun sendMails(vararg messages: AppMimeMessage) {
        messages.forEach { it -> log.info("\nMessage sent to  ${it.to}.\n Subject: ${it.subject}\n Message: ${it.text}\n Attachments size: ${it.attachments.size}") }
    }

    val log = LoggerFactory.getLogger(FakeAppMailSender::class.java)!!
}