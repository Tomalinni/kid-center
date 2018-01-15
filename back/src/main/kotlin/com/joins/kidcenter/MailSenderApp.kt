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

package com.joins.kidcenter

import com.joins.kidcenter.utils.PropertiesLoader
import com.joins.kidcenter.utils.toInt
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper

@SpringBootApplication(exclude = arrayOf(FlywayAutoConfiguration::class))
open class MailSenderApp {

    var profile = "default"
    var to = ""
    var subject = "test"
    var text = "test message"
    var from = ""

    val mailSender = JavaMailSenderImpl()

    companion object {
        val profileArgPrefix = "--profile="
        val validProfiles = arrayOf("default", "prod")

        @JvmStatic fun main(args: Array<String>) {
            val app = MailSenderApp()
            if (app.init(args)) {
                app.sendMail()
            }
        }
    }

    fun init(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            println("Usage: MailSenderAppMailSenderApp --profile={default|prod} [to subject text]")
            return false
        }

        val parsedProfile = args[0].substring(profileArgPrefix.length)
        if (!validProfiles.contains(parsedProfile)) {
            println("Profile $parsedProfile is invalid. Valid profile should be one of ${validProfiles.joinToString(", ")}")
            return false
        }
        profile = parsedProfile
        if (args.size > 1) {
            to = args[1]
        }
        if (args.size > 2) {
            subject = args[2]
        }
        if (args.size > 3) {
            text = args[3]
        }

        val appProps = PropertiesLoader.load(profile)

        mailSender.apply {
            host = appProps["app.mail.sender.host"].toString()
            port = appProps["app.mail.sender.port"].toString().toInt(25)
            username = appProps["app.mail.sender.username"].toString()
            password = appProps["app.mail.sender.password"].toString()
            protocol = appProps["app.mail.sender.protocol"].toString()
            javaMailProperties.put("mail.smtp.auth", true)
            javaMailProperties.put("mail.smtp.starttls.enable", true)

            from = appProps["app.mail.sender.from"].toString()
        }

        return true
    }

    fun sendMail() {
        println("Starting send mail. Check $to for letter.")

        val mimeMsg = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMsg, true)

        helper.setFrom(from)
        helper.setTo(arrayOf(to))
        helper.setSubject(subject)
        helper.setText(text, true)


        mailSender.send(mimeMsg)
    }
}
