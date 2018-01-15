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

package com.joins.kidcenter.utils

import com.joins.kidcenter.service.lessons.LessonSlotService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.temporal.ChronoUnit

class LoggingRunnableDecorator(val taskName: String, val runnable: Runnable) : Runnable {
    val log: Logger = LoggerFactory.getLogger(LessonSlotService::class.java)

    override fun run() {
        val start = DateTimeUtils.currentDateTime()
        log.info("Task '$taskName' started at $start")
        try {
            runnable.run()
            val finish = DateTimeUtils.currentDateTime()
            log.info("Task '$taskName' finished at $finish, duration: ${start.until(finish, ChronoUnit.SECONDS)}")

        } catch(e: Throwable) {
            val finish = DateTimeUtils.currentDateTime()
            log.error("Task '$taskName' finished with Error at $finish, duration: ${start.until(finish, ChronoUnit.SECONDS)}, error: ${e.message}")
            throw e
        }
    }
}