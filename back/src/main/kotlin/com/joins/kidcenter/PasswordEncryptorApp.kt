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

package com.joins.kidcenter

import com.joins.kidcenter.utils.CheckSums
import com.joins.kidcenter.utils.PropertiesLoader

open class PasswordEncryptorApp {

    var profile = "default"
    var passwordsToEncrypt = arrayOf<String>()
    var salt = ""

    companion object {
        val profileArgPrefix = "--profile="
        val validProfiles = arrayOf("default", "prod")

        @JvmStatic fun main(args: Array<String>) {
            val app = PasswordEncryptorApp()
            if (app.init(args)) {
                app.encryptPasswords()
            }
        }
    }

    fun init(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            println("Usage: PasswordEncryptorApp [--profile={default|prod}] password [password]*")
            return false
        }

        if (args[0].startsWith(profileArgPrefix)) {
            val parsedProfile = args[0].substring(profileArgPrefix.length)
            if (!validProfiles.contains(parsedProfile)) {
                println("Profile $parsedProfile is invalid. Valid profile should be one of ${validProfiles.joinToString(", ")}")
                return false
            }
            profile = parsedProfile
            passwordsToEncrypt = args.copyOfRange(1, args.size)
        } else {
            passwordsToEncrypt = args
        }

        val appProps = PropertiesLoader.load(profile)
        salt = appProps["pass.salt"] as String? ?: ""

        return true
    }

    fun encryptPasswords() {
        println("Starting encryption. Profile: $profile, Salt: $salt")
        passwordsToEncrypt.forEach {
            val encrypted = CheckSums.sha512(it, salt)
            println("$it -> $encrypted")
        }
    }
}

