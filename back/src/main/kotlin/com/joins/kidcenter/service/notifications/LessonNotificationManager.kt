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

package com.joins.kidcenter.service.notifications

import com.joins.kidcenter.service.lessons.LessonEvent
import com.joins.kidcenter.service.lessons.LessonEventListener
import com.joins.kidcenter.service.sms.scheduling.SmsSchedulingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface LessonNotificationManager : LessonEventListener

@Service
@Transactional
open class LessonNotificationManagerImpl @Autowired constructor(
        val smsSchedulingService: SmsSchedulingService
) : LessonNotificationManager {

    override fun onLessonEvent(event: LessonEvent) {
        smsSchedulingService.registerLessonEvent(event)
    }
}