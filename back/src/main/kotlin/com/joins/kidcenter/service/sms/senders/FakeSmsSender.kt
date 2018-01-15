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

package com.joins.kidcenter.service.sms.senders

import com.joins.kidcenter.service.sms.SmsPersonalizedMessage
import com.joins.kidcenter.service.sms.TemplatedMessage
import com.joins.kidcenter.service.sms.scheduling.TimeScope
import com.joins.kidcenter.utils.VerificationCodeGenerator
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory


class FakeSmsSender : SmsSender {
    val log = LoggerFactory.getLogger(FakeSmsSender::class.java)

    val mobilePrefix = "100000000"
    val statuses: MutableMap<String, SmsProviderOperationStatus> = mutableMapOf()

    init {
        val availableStatuses: Array<SmsProviderOperationStatus> = SmsProviderOperationStatus.values()
        (0..availableStatuses.size - 1).forEach {
            statuses.put(mobilePrefix + StringUtils.leftPad("$it", 2, '0'), availableStatuses[it])
        }
    }

    override fun sendConfirmMobileCode(mobile: String): SmsProviderResponse<VerificationCodeResponseData> {
        val verificationCode = VerificationCodeGenerator.generate()
        return fakeSendMessage(mobile, TemplatedMessage("confirmMobile", listOf(verificationCode)), { VerificationCodeResponseData(verificationCode) })
    }

    override fun sendPlannedLessonNotifications(messages: Collection<SmsPersonalizedMessage>, timeScope: TimeScope): List<SmsProviderResponse<BaseResponseData>> {
        return fakeSendMessages(messages) {
            fakeSendMessage(it.mobile, TemplatedMessage("plannedLessons", it.arguments), { BaseResponseData() })
        }
    }

    override fun sendChangedLessonNotifications(messages: Collection<SmsPersonalizedMessage>): List<SmsProviderResponse<BaseResponseData>> {
        return fakeSendMessages(messages) {
            fakeSendMessage(it.mobile, TemplatedMessage("changedLessons", it.arguments), { BaseResponseData() })
        }
    }

    private fun <T> fakeSendMessages(messages: Collection<SmsPersonalizedMessage>, sendAction: (SmsPersonalizedMessage) -> SmsProviderResponse<T>): List<SmsProviderResponse<T>> {
        if (messages.isEmpty()) {
            log.warn("Empty messages collection can not be sent, request is ignored")
        }
        return messages.map(sendAction)
    }

    private fun <T> fakeSendMessage(mobile: String, message: TemplatedMessage, responseDataFactory: () -> T): SmsProviderResponse<T> {
        val messageString = message.toString()
        log.info("Message to be sent:\n$messageString")

        val providerResponse = SmsProviderResponse(statuses[mobile] ?: SmsProviderOperationStatus.success, responseDataFactory())
        logResponse(providerResponse.status, mobile, null)
        return providerResponse
    }

    private fun logResponse(status: SmsProviderOperationStatus, mobile: String, additionalMessage: String?) {
        if (status == SmsProviderOperationStatus.success) {
            log.info("Virtual sms message was sent to $mobile" + if (additionalMessage.isNullOrBlank()) "" else additionalMessage)
        } else {
            log.info("Virtual sms message was NOT sent to $mobile. Status is $status")
        }
    }
}
