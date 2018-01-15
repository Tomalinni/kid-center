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
import com.joins.kidcenter.service.sms.senders.SmsProviderOperationStatus.*
import com.joins.kidcenter.utils.VerificationCodeGenerator
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import java.util.*

class SmsSenderCr6868 : SmsSender {

    private val statusCodeMapper = SmsProviderCr6868OperationCodeMapper()

    @Value("\${app.sms.sender.cr6868.url}")
    private val providerUrl: String? = null

    @Value("\${app.sms.sender.cr6868.name}")
    private val accountName: String? = null

    @Value("\${app.sms.sender.cr6868.pwd}")
    private val accountPwd: String? = null

    override fun sendConfirmMobileCode(mobile: String): SmsProviderResponse<VerificationCodeResponseData> {
        try {
            val verificationCode = VerificationCodeGenerator.generate()
            val responseBody = sendMessage(mobile, verificationCode)

            println(responseBody)
            val firstSeparatorIndex = responseBody.indexOf(",")
            if (firstSeparatorIndex < 0) {
                return error(providerConnectionError)
            }
            val statusCode = responseBody.substring(0, firstSeparatorIndex)
            return SmsProviderResponse(statusCodeMapper.map(statusCode), VerificationCodeResponseData(verificationCode))
        } catch (e: IOException) {
            return error(providerConnectionError)
        }

    }

    override fun sendPlannedLessonNotifications(messages: Collection<SmsPersonalizedMessage>, timeScope: TimeScope): List<SmsProviderResponse<BaseResponseData>> {
        throw NotImplementedError()
    }

    override fun sendChangedLessonNotifications(messages: Collection<SmsPersonalizedMessage>): List<SmsProviderResponse<BaseResponseData>> {
        throw NotImplementedError()
    }

    @Throws(IOException::class)
    private fun sendMessage(mobile: String, data: String): String {
        val httpClient = HttpClientBuilder.create().build()
        val httpPost = HttpPost(providerUrl)

        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")

        val nvps = ArrayList<NameValuePair>()
        nvps.add(BasicNameValuePair("name", accountName))
        nvps.add(BasicNameValuePair("pwd", accountPwd))
        nvps.add(BasicNameValuePair("mobile", mobile))
        nvps.add(BasicNameValuePair("content", data))
        nvps.add(BasicNameValuePair("sign", "kid-center"))
        nvps.add(BasicNameValuePair("type", "pt"))
        nvps.add(BasicNameValuePair("extno", ""))
        httpPost.entity = UrlEncodedFormEntity(nvps, "utf-8")

        val response = httpClient.execute(httpPost)

        return EntityUtils.toString(response.entity, "utf-8")
    }

    private fun error(status: SmsProviderOperationStatus): SmsProviderResponse<VerificationCodeResponseData> {
        return SmsProviderResponse(status, VerificationCodeResponseData(""))
    }
}

class SmsProviderCr6868OperationCodeMapper : SmsProviderOperationCodeMapper {
    private val codesToResult = HashMap<String, SmsProviderOperationStatus>()

    init {
        codesToResult.put("0", success)
        codesToResult.put("1", invalidMessage)
        codesToResult.put("2", noMoney)
        codesToResult.put("3", invalidNumber)
        codesToResult.put("4", invalidMessage)
        codesToResult.put("10", accountNotExist)
        codesToResult.put("11", notAuthorized)
        codesToResult.put("12", accountBlocked)
        codesToResult.put("13", notAuthorized)
        codesToResult.put("14", malformedRequest)
    }

    override fun map(code: String): SmsProviderOperationStatus {
        val result = codesToResult[code]
        return result ?: unknownError
    }
}