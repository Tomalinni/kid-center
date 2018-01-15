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
import com.joins.kidcenter.service.sms.scheduling.TimeScope

interface SmsSender {

    fun sendConfirmMobileCode(mobile: String): SmsProviderResponse<VerificationCodeResponseData>

    fun sendPlannedLessonNotifications(messages: Collection<SmsPersonalizedMessage>, timeScope: TimeScope): List<SmsProviderResponse<BaseResponseData>>

    fun sendChangedLessonNotifications(messages: Collection<SmsPersonalizedMessage>): List<SmsProviderResponse<BaseResponseData>>
}

interface SmsProviderOperationCodeMapper {
    fun map(code: String): SmsProviderOperationStatus
}

class SmsProviderResponse<out T>(val status: SmsProviderOperationStatus, val data: T)

enum class SmsProviderOperationStatus {
    success,
    invalidMessage,
    noMoney,
    invalidNumber,
    accountNotExist,
    notAuthorized,
    accountBlocked,
    malformedRequest,
    providerConnectionError,

    unknownError
}

open class BaseResponseData(val stausMessage: String = "")

class VerificationCodeResponseData(val code: String, stausMessage: String = "") : BaseResponseData(stausMessage)