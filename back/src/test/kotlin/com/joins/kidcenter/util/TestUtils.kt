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

package com.joins.kidcenter.util

import org.junit.Assert

object TestUtils {

    fun assertException(block: () -> Any, exceptionAssert: (e: Throwable) -> Unit) {
        var exceptionThrown = false
        try {
            block.invoke()
        } catch (e: Throwable) {
            exceptionThrown = true
            exceptionAssert.invoke(e)
        }
        if (!exceptionThrown) {
            Assert.fail("Some exception is supposed to be thrown, but it was NOT")
        }
    }
}