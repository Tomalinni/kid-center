package com.joins.kidcenter.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
open class PasswordEncryptor {

    @Value("\${pass.salt}")
    var salt: String = ""

    fun encrypt(value: String): String {
        return CheckSums.sha512(value, salt)
    }
}