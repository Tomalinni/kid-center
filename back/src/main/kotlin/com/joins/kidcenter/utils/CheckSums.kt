package com.joins.kidcenter.utils

import java.security.MessageDigest

object CheckSums {
    @JvmStatic
    fun sha1(appSecret: String, nonce: String, curTime: String): String {
        return encode("sha1", appSecret + nonce + curTime)
    }

    @JvmStatic
    fun sha512(secret: String, salt: String): String {
        return encode("SHA-512", salt + secret + salt)
    }

    @JvmStatic
    private fun sha512(secret: String): String {
        return encode("SHA-512", secret)
    }

    @JvmStatic
    fun md5(requestBody: String): String {
        return encode("md5", requestBody)
    }

    private fun encode(algorithm: String, value: String): String {
        try {
            val messageDigest = MessageDigest.getInstance(algorithm)
            messageDigest.update(value.toByteArray())
            return hex2String(messageDigest.digest())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    private fun hex2String(bytes: ByteArray): String {
        val len = bytes.size
        val buf = StringBuilder(len * 2)
        for (b in bytes) {
            buf.append(HEX_DIGITS[b.toInt() shr 4 and 0x0f])
            buf.append(HEX_DIGITS[b.toInt() and 0x0f])
        }
        return buf.toString()
    }

    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
}
