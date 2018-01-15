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
import com.joins.kidcenter.service.sms.TemplatedMessage
import com.joins.kidcenter.service.sms.scheduling.TimeScope
import com.joins.kidcenter.utils.CheckSums
import com.joins.kidcenter.utils.VerificationCodeGenerator
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import sun.misc.BASE64Encoder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Suppress("DEPRECATION")
@Profile("prod")
@Component
open class SmsSenderYuntongxun
@Autowired constructor(val objectMapper: ObjectMapper) : SmsSender {

    val log: Logger = LoggerFactory.getLogger(SmsSenderYuntongxun::class.java)

    private val providerDatePattern = "yyyyMMddHHmmss"
    private val successCode = "000000"

    @Value("\${app.sms.sender.ytx.host}")
    private var providerHost: String = ""

    @Value("\${app.sms.sender.ytx.port}")
    private var providerPort: Int = -1

    @Value("\${app.sms.sender.ytx.apiVersion}")
    private var providerApiVersion: String = ""

    @Value("\${app.sms.sender.ytx.accSid}")
    private var accountSid: String = ""

    @Value("\${app.sms.sender.ytx.accToken}")
    private var accountToken: String = ""

    @Value("\${app.sms.sender.ytx.appId}")
    private var appId: String = ""

    @Value("\${app.sms.sender.ytx.templateId.confirm.mobile}")
    private var confirmMobileTemplateId: String = ""

    @Value("\${app.sms.sender.ytx.templateId.planned.lessons.week}")
    private var plannedLessonsWeekTemplateId: String = ""

    @Value("\${app.sms.sender.ytx.templateId.planned.lessons.next.day}")
    private var plannedLessonsNextDayTemplateId: String = ""

    @Value("\${app.sms.sender.ytx.templateId.planned.lessons.current.day}")
    private var plannedLessonsCurrentDayTemplateId: String = ""

    @Value("\${app.sms.sender.ytx.templateId.changed.lessons}")
    private var changedLessonsTemplateId: String = ""


    override fun sendConfirmMobileCode(mobile: String): SmsProviderResponse<VerificationCodeResponseData> {
        val verificationCode = VerificationCodeGenerator.generate()
        val response = sendMessageAndHandleResponse(mobile, TemplatedMessage(confirmMobileTemplateId, listOf(verificationCode)))
        return SmsProviderResponse(response.status, VerificationCodeResponseData(verificationCode, response.data.stausMessage))
    }

    override fun sendPlannedLessonNotifications(messages: Collection<SmsPersonalizedMessage>, timeScope: TimeScope): List<SmsProviderResponse<BaseResponseData>> {
        return sendMessages(messages) {
            sendMessageAndHandleResponse(it.mobile, TemplatedMessage(selectTemplate(timeScope), it.arguments))
        }
    }

    override fun sendChangedLessonNotifications(messages: Collection<SmsPersonalizedMessage>): List<SmsProviderResponse<BaseResponseData>> {
        return sendMessages(messages) {
            sendMessageAndHandleResponse(it.mobile, TemplatedMessage(changedLessonsTemplateId, it.arguments))
        }
    }

    private fun selectTemplate(timeScope: TimeScope): String {
        return when (timeScope) {
            TimeScope.week -> plannedLessonsWeekTemplateId
            TimeScope.nextDay -> plannedLessonsNextDayTemplateId
            TimeScope.currentDay -> plannedLessonsCurrentDayTemplateId
        }

    }

    private fun sendMessages(messages: Collection<SmsPersonalizedMessage>, sendAction: (SmsPersonalizedMessage) -> SmsProviderResponse<BaseResponseData>): List<SmsProviderResponse<BaseResponseData>> {
        if (messages.isEmpty()) {
            log.warn("Empty messages collection can not be sent, request is ignored")
        }
        return messages.map(sendAction).toList()
    }

    private fun sendMessageAndHandleResponse(mobile: String, message: TemplatedMessage): SmsProviderResponse<BaseResponseData> {
        try {
            val responseBody = sendMessage(mobile, message)
            log.info("Response received: $responseBody")
            val providerResponse = objectMapper.readValue(responseBody, SmsSendYtxResponse::class.java)

            if (providerResponse.statusCode == successCode) {
                return SmsProviderResponse(SmsProviderOperationStatus.success, BaseResponseData())
            } else {
                log.error("Unsuccessful response: $providerResponse")
                return SmsProviderResponse(SmsProviderOperationStatus.unknownError, BaseResponseData(providerResponse.statusMsg ?: ""))
            }
        } catch (e: IOException) {
            log.error("Sending error ${e.message}", e)
            return SmsProviderResponse(SmsProviderOperationStatus.providerConnectionError, BaseResponseData(e.message!!))
        } catch (e: Exception) {
            log.error("Sending error ${e.message}", e)
            return SmsProviderResponse(SmsProviderOperationStatus.unknownError, BaseResponseData(e.message!!))
        }
    }

    @Throws(IOException::class)
    private fun sendMessage(mobile: String, message: TemplatedMessage): String {
        val httpClient = createHttpClient(providerPort)
        val httpPost = getHttpRequestBase() as HttpPost

        val smsSendRequest = SmsSendYtxRequest(appId, mobile, message.templateId, message.arguments.toTypedArray())
        val body = objectMapper.writeValueAsString(smsSendRequest)


        log.info("Sending request: $body")
        val httpEntity = BasicHttpEntity()
        httpEntity.content = ByteArrayInputStream(body.toByteArray(charset("utf-8")))
        httpEntity.contentLength = body.toByteArray(charset("utf-8")).size.toLong()
        httpPost.entity = httpEntity
        val response = httpClient.execute(httpPost)

        return EntityUtils.toString(response.entity, "utf-8")
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    private fun getHttpRequestBase(): HttpRequestBase {
        val timestamp = SimpleDateFormat(providerDatePattern).format(Date())
        val signature = CheckSums.md5("$accountSid$accountToken$timestamp").toUpperCase()
        val url = "${providerUrl()}/Accounts/$accountSid/SMS/TemplateSMS?sig=$signature"

        val reqBase = HttpPost(url)
        reqBase.setHeader("Accept", "application/json")
        reqBase.setHeader("Content-Type", "application/json;charset=utf-8")
        reqBase.setHeader("Authorization", BASE64Encoder().encode("$accountSid:$timestamp".toByteArray()))
        return reqBase
    }

    private fun providerUrl() =
            "https://$providerHost:$providerPort/$providerApiVersion"

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    fun createHttpClient(port: Int): DefaultHttpClient {
        try {
            val httpClient = DefaultHttpClient()
            val ctx = SSLContext.getInstance("TLS")

            val tm = object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {
                    if (chain == null || chain.isEmpty())
                        throw IllegalArgumentException("null or zero-length certificate chain")
                    if (authType == null || authType.isEmpty())
                        throw IllegalArgumentException("null or zero-length authentication type")
                    if (!chain.any { it.subjectX500Principal != null })
                        throw CertificateException("Server certificate validation failed")
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }

            }
            ctx.init(null, arrayOf<TrustManager>(tm), SecureRandom())

            val socketFactory = SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
            val scheme = Scheme("https", port, socketFactory)

            httpClient.connectionManager.schemeRegistry.register(scheme)
            return httpClient
        } catch (e: Exception) {
            throw RuntimeException("Error creating HTTPS client", e)
        }
    }

    @Suppress("unused")
    class SmsSendYtxRequest(val appId: String,
                            val to: String,
                            val templateId: String,
                            val datas: Array<String>)

    class SmsSendYtxResponse(var statusCode: String,
                             var statusMsg: String?) {
        override fun toString(): String {
            return "SmsSendYtxResponse(statusCode='$statusCode', statusMsg=$statusMsg)"
        }
    }
}