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

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.service.sms.SmsPersonalizedMessage
import com.joins.kidcenter.service.sms.scheduling.TimeScope
import com.joins.kidcenter.service.sms.senders.SmsProviderOperationStatus.*
import com.joins.kidcenter.utils.CheckSums
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import java.util.*

//@Component
open class SmsSenderNetease : SmsSender {

    private val statusCodeMapper = SmsProviderNeteaseOperationCodeMapper()

    @Autowired
    private val mapper: ObjectMapper? = null

    @Value("\${app.sms.sender.netease.url}")
    private val providerUrl: String? = null

    @Value("\${app.sms.sender.netease.key}")
    private val appKey: String? = null

    @Value("\${app.sms.sender.netease.secret}")
    private val appSecret: String? = null

    @Value("\${app.sms.sender.netease.nonce}")
    private val nonce: String? = null

    override fun sendConfirmMobileCode(mobile: String): SmsProviderResponse<VerificationCodeResponseData> {
        try {
            val httpClient = HttpClientBuilder.create().build()
            val httpPost = HttpPost(providerUrl)

            val curTime = (Date().time / 1000L).toString()
            val checkSum = CheckSums.sha1(appSecret!!, nonce!!, curTime)

            httpPost.addHeader("AppKey", appKey)
            httpPost.addHeader("Nonce", nonce)
            httpPost.addHeader("CurTime", curTime)
            httpPost.addHeader("CheckSum", checkSum)
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")

            val nvps = ArrayList<NameValuePair>()
            nvps.add(BasicNameValuePair("mobile", mobile))
            httpPost.entity = UrlEncodedFormEntity(nvps, "utf-8")

            val response = httpClient.execute(httpPost)

            val responseBody = EntityUtils.toString(response.entity, "utf-8")
            println(responseBody)

            val root = mapper!!.readTree(responseBody)

            val statusCode = root.path("code").asText()
            val verificationCode = root.get("obj").asText()
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

    private fun error(status: SmsProviderOperationStatus): SmsProviderResponse<VerificationCodeResponseData> {
        return SmsProviderResponse(status, VerificationCodeResponseData(""))
    }
}

class SmsProviderNeteaseOperationCodeMapper : SmsProviderOperationCodeMapper {
    private val codesToResult = HashMap<String, SmsProviderOperationStatus>()

    init {
        codesToResult.put("200", success)
        codesToResult.put("403", notAuthorized)
        codesToResult.put("414", malformedRequest)
    }

    override fun map(code: String): SmsProviderOperationStatus {
        val result = codesToResult[code]
        return result ?: unknownError
    }
}