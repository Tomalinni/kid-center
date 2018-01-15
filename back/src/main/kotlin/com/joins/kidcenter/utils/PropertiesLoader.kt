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

package com.joins.kidcenter.utils

import org.apache.commons.lang3.Validate
import java.util.*

object PropertiesLoader {

    fun load(profileName: String): Properties {
        val appProps = Properties()
        val resourceName = "application-$profileName.properties"
        val resourceStream = this.javaClass.classLoader.getResourceAsStream(resourceName)
        Validate.notNull(resourceStream, "Can not find resource $resourceName. Invalid profile name $profileName")
        appProps.load(resourceStream)
        return appProps
    }
}
