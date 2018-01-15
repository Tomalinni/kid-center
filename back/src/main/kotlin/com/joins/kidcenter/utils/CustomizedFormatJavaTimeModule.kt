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

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.PackageVersion
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.joins.kidcenter.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CustomizedFormatJavaTimeModule : SimpleModule(PackageVersion.VERSION) {
    init {
        val dateFormatter = DateTimeFormatter.ofPattern(Config.datePattern)
        val timeFormatter = DateTimeFormatter.ofPattern(Config.timePattern)
        val dateTimeFormatter = DateTimeFormatter.ofPattern(Config.datetTimePattern)

        //deserializers:
        addDeserializer(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
        addDeserializer(LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))

        //serializers:
        addSerializer(LocalDate::class.java, LocalDateSerializer(dateFormatter))
        addSerializer(LocalTime::class.java, LocalTimeSerializer(timeFormatter))
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
    }

    companion object {
        private val serialVersionUID = 1L
    }
}