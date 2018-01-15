package com.joins.kidcenter

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration

@SpringBootApplication(exclude = arrayOf(FlywayAutoConfiguration::class))
open class KidCenterApplication {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(KidCenterApplication::class.java, *args)
        }
    }
}
