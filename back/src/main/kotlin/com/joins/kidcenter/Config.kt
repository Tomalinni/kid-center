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

import java.time.LocalDate

object Config {
    val datePattern = "dd.MM.yyyy"
    val timePattern = "HH:mm"
    val datetTimePattern = "dd.MM.yyyy-HHmm"
    val maxSlotsHardLimit: Int = 10
    val currentTimeZone = "Etc/GMT-8"
    val schoolNameEn = "ANNASALENKO"
    val schoolContactNumber = "18318343170"
    val paidSupportContactNumber = "17701928381"
    val commonValidationMessage = "_"
    val minDate = LocalDate.of(2000, 1, 1)
    val maxDate = LocalDate.of(2100, 1, 1)
    val minStudentAgeYears = 0.5
    val maxStudentAgeYears = 10
    //Effective max age is 7, but sometimes it is needed to modify information about old students
    val nextLessonsRangeDays = 35L
    val nextLessonsLookupCount = 4
    val smsConfirmationExpirationTimeMin = 60
    val smsConfirmationAttemptsCountLimit = 20
    val anonymouslyPermittedPaths = arrayOf("/data/auth/**", "/data/sms/verify/**")
    val relativeUserRoleId = "relative"
    val defaultStudentRelativeRoleId = 1L
    const val paymentMonthSumRefreshPeriod = 60 * 60 * 1000L
    val authSchemePrefix = "Bearer "
    val backDatePlanningEnabled = true
    var backDateRevokeEnabled = false
    var schoolVisitsTrackingEnabled = false
    val lessonsChangeNotificationSendDelayMins = 0.1
    val lessonsChangeLookAheadDays = 7
    val currentDayLessonsNotificationGapMins = 60L
}

object AppMimeTypes {
    val xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}
