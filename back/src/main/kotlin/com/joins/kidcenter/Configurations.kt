package com.joins.kidcenter

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.joins.kidcenter.security.config.WebSecurityConfig
import com.joins.kidcenter.service.StudentStatusManager
import com.joins.kidcenter.service.lessons.LessonEventListener
import com.joins.kidcenter.service.lessons.LessonEventListeners
import com.joins.kidcenter.service.mail.AppMailSender
import com.joins.kidcenter.service.mail.AppMailSenderImpl
import com.joins.kidcenter.service.mail.FakeAppMailSender
import com.joins.kidcenter.service.notifications.LessonNotificationManager
import com.joins.kidcenter.service.sms.senders.FakeSmsSender
import com.joins.kidcenter.service.sms.senders.SmsSender
import com.joins.kidcenter.service.sms.senders.SmsSenderYuntongxun
import com.joins.kidcenter.utils.CustomizedFormatJavaTimeModule
import com.joins.kidcenter.utils.audit.Auditor
import com.joins.kidcenter.utils.audit.ZonedDateTimeProvider
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.web.MultipartProperties
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.context.annotation.*
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import javax.sql.DataSource


@Configuration
@Profile("prod")
open class ProductionConfiguration {

    @Bean
    open fun smsSender(sender: SmsSenderYuntongxun): SmsSender {
        return sender
    }

    @Bean
    open fun mailSender(sender: AppMailSenderImpl): AppMailSender {
        return sender
    }
}

@Configuration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = arrayOf(ComponentScan.Filter(type = FilterType.CUSTOM, classes = arrayOf(TypeExcludeFilter::class)), ComponentScan.Filter(type = FilterType.CUSTOM, classes = arrayOf(AutoConfigurationExcludeFilter::class))))
@EnableScheduling
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@Import(WebSecurityConfig::class)
open class CommonConfiguration {

    @Value("\${flyway.enabled}")
    private var migrateAtStart: Boolean = true

    @Bean
    open fun flyway(@Suppress("SpringKotlinAutowiring") dataSource: DataSource): Flyway {
        val flyway = Flyway()
        flyway.dataSource = dataSource
        if (migrateAtStart) {
            flyway.repair()
            flyway.migrate()
        }
        return flyway
    }

    @Bean
    open fun smsSender(): SmsSender {
        return FakeSmsSender()
    }

    @Bean
    open fun mailSender(): AppMailSender {
        return FakeAppMailSender()
    }

    @Bean
    open fun jacksonBuilder(): Jackson2ObjectMapperBuilder {
        val b = Jackson2ObjectMapperBuilder()
        b.modules(CustomizedFormatJavaTimeModule(), KotlinModule())
        return b
    }

    @Bean
    open fun multipartProperties(): MultipartProperties = MultipartProperties().apply {
        maxFileSize = "25MB"
        maxRequestSize = "25MB"
    }

    @Bean
    open fun auditorProvider(): AuditorAware<String> {
        return Auditor()
    }

    @Bean
    open fun dateTimeProvider(): DateTimeProvider {
        return ZonedDateTimeProvider()
    }

    @Bean
    open fun getFreeMarkerConfiguration(): FreeMarkerConfigurer {
        val freemarker = FreeMarkerConfigurer()
        freemarker.setTemplateLoaderPath("classpath:freemarker/")
        freemarker.setDefaultEncoding("UTF-8")
        return freemarker
    }

    @Bean
    @Qualifier("lessonEventListeners")
    open fun getLessonEventListener(studentStatusManager: StudentStatusManager,
                                    lessonNotificationManager: LessonNotificationManager): LessonEventListener {
        return LessonEventListeners(listOf(studentStatusManager, lessonNotificationManager))
    }

    @Bean
    @Qualifier("closeLessonScheduler")
    open fun getCloseLessonScheduler(): TaskScheduler {
        return ThreadPoolTaskScheduler()
    }
}

