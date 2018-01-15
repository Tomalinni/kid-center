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

package com.joins.kidcenter.controller

import com.joins.kidcenter.service.maps.MobileConfirmationService
import com.joins.kidcenter.service.sms.senders.SmsProviderOperationStatus
import com.joins.kidcenter.service.sms.senders.SmsProviderResponse
import com.joins.kidcenter.service.sms.senders.SmsSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("data/sms")
class SmsController @Autowired constructor(
        val smsSender: SmsSender,
        val confirmationService: MobileConfirmationService) {


    @RequestMapping(path = arrayOf("/verify/{mobile}"), method = arrayOf(RequestMethod.POST))
    fun verifyMobile(@PathVariable mobile: String): SmsProviderResponse<String> {
        //action is enabled for anonymous
        val response = smsSender.sendConfirmMobileCode(mobile)
        val confirmationId = if (response.status == SmsProviderOperationStatus.success) confirmationService.createConfirmation(mobile, response.data.code) else ""

        return SmsProviderResponse(response.status, confirmationId)
    }

}
