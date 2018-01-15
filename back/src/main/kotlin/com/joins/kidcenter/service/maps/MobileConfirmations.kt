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

package com.joins.kidcenter.service.maps

import com.joins.kidcenter.Config
import com.joins.kidcenter.controller.OperationResponse
import com.joins.kidcenter.utils.DateTimeUtils
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct
import kotlin.concurrent.withLock

class MobileConfirmation(val phoneNumber: String,
                         val expectedConfirmationCode: String) {

    var expirationTime: LocalDateTime = nextExpirationTime()
    var confirmAttemptsCount = 0

    fun refreshExpirationTime() {
        expirationTime = nextExpirationTime()
    }

    private fun nextExpirationTime() = DateTimeUtils.currentDateTime().plusMinutes(Config.smsConfirmationExpirationTimeMin.toLong())
}

@Component
open class MobileConfirmationService() {

    private val confirmationsById: MutableMap<String, MobileConfirmation> = mutableMapOf()
    private val idsByNumber: MutableMap<String, String> = mutableMapOf()
    private val lock: Lock = ReentrantLock(true)

    @PostConstruct
    private fun init() {
        Timer(true).scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                removeExpiredConfirmations()
            }
        }, 500L, (Config.smsConfirmationExpirationTimeMin * 60 * 1000).toLong())
    }


    private fun removeExpiredConfirmations() {
        lock.withLock {
            val idsIter = idsByNumber.iterator()
            while (idsIter.hasNext()) {
                val idsEntry = idsIter.next()
                val confirmationId = idsEntry.value
                val confirmation = confirmationsById[confirmationId]
                if (confirmation == null || confirmation.expirationTime.isBefore(DateTimeUtils.currentDateTime())) {
                    if (confirmation != null) confirmationsById.remove(confirmationId)
                    idsIter.remove()
                }
            }
        }
    }

    /**
     * Method is invoked after sms has been successfully sent, phone number is assumed to be valid
     */
    fun createConfirmation(phoneNumber: String, expectedCode: String): String {
        lock.withLock {
            var confirmationId = idsByNumber[phoneNumber]
            if (confirmationId == null) confirmationId = UUID.randomUUID().toString()
            idsByNumber[phoneNumber] = confirmationId
            confirmationsById[confirmationId] = MobileConfirmation(phoneNumber, expectedCode)
            return confirmationId
        }
    }

    fun checkConfirmation(confirmationId: String, phoneNumber: String, enteredCode: String): OperationResponse {
        lock.withLock {
            if (confirmationId.isBlank()) return OperationResponse.error("common.sms.confirmation.incorrect.code")
            val confirmation = confirmationsById[confirmationId]
            if (confirmation == null || confirmation.expirationTime.isBefore(DateTimeUtils.currentDateTime())) return OperationResponse.error("common.sms.confirmation.expired")
            if (confirmation.confirmAttemptsCount >= Config.smsConfirmationAttemptsCountLimit) return OperationResponse.error("common.sms.confirmation.attempts.count.limit.exceeded")
            confirmation.confirmAttemptsCount++

            if (confirmation.phoneNumber != phoneNumber || confirmation.expectedConfirmationCode != enteredCode) return OperationResponse.error("common.sms.confirmation.incorrect.code")

            // Checks are passed for this specific number.
            // Nevertheless confirmation can be checked next time if there were other validation errors, so do not remove confirmation from map.
            // Refresh its expiration time to give user the same amount of time in case of errors.
            confirmation.refreshExpirationTime()

            return OperationResponse.success()
        }
    }

}
