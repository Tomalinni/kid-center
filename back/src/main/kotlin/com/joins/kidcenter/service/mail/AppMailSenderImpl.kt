package com.joins.kidcenter.service.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct
import javax.mail.internet.MimeMessage

@Profile("prod", "default")
@Component
class AppMailSenderImpl : AppMailSender {

    val mailSender = JavaMailSenderImpl()

    @Value("\${app.mail.sender.host}")
    private var host: String = ""

    @Value("\${app.mail.sender.port}")
    private var port: Int = 0

    @Value("\${app.mail.sender.username}")
    private var username: String = ""

    @Value("\${app.mail.sender.password}")
    private var password: String = ""

    @Value("\${app.mail.sender.protocol}")
    private var protocol: String = ""

    @Value("\${app.mail.sender.from}")
    private var from: String = ""

    private var prepared = false

    @PostConstruct
    fun prepareSender() {
        if (prepared) return

        mailSender.host = host
        mailSender.port = port
        mailSender.username = username
        mailSender.password = password
        mailSender.protocol = protocol
        mailSender.javaMailProperties.put("mail.smtp.auth", true)
        mailSender.javaMailProperties.put("mail.smtp.starttls.enable", true)
        prepared = true
    }

    fun generateMessage(message: AppMimeMessage): MimeMessage {
        val mimeMsg = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMsg, true)

        helper.setFrom(from)
        helper.setTo(message.to.toTypedArray())
        helper.setSubject(message.subject)
        helper.setText(message.text, true)

        message.attachments.forEach { a -> helper.addAttachment(a.name, a) }
        return mimeMsg
    }

    override fun sendMails(vararg messages: AppMimeMessage) {
        mailSender.send(*messages.map { generateMessage(it) }.toTypedArray())
    }

}