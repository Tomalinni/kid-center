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

package com.joins.kidcenter.utils.imports

class Aggregator {
    private var sum: Double = 0.0
    private var count: Double = 0.0
    private var max: Double = 0.0
    private var min: Double = 0.0
    private var avg: Double = 0.0

    fun add(value: Double) {
        sum += value
        count++
        avg = sum.toDouble() / count
        max = Math.max(max, value)
        min = Math.min(min, value)
    }

    fun max(): Double = max
    fun min(): Double = min
    fun avg(): Double = avg
    fun sum(): Double = sum
    fun count(): Double = count
}
