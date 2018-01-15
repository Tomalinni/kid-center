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

package com.joins.kidcenter.service.templates

import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

interface TemplateRenderer {
    fun renderTemplate(category: TemplateCategory, templateName: String, model: Any): String
}

interface TemplateDescriptor {
    fun render(renderer: TemplateRenderer): String
}

abstract class AbstractTemplateDescriptor<T>(
        private val input: T,
        private val category: TemplateCategory,
        private val templateName: String) : TemplateDescriptor {

    override fun render(renderer: TemplateRenderer): String {
        return renderer.renderTemplate(category, templateName, createModel(input))
    }

    abstract fun createModel(input: T): Any
}

enum class TemplateCategory {
    sms, mail
}

@Component
open class TemplateRendererImpl @Autowired constructor(
        val freemarker: Configuration
) : TemplateRenderer {
    val log = LoggerFactory.getLogger(TemplateRendererImpl::class.java)!!

    override fun renderTemplate(category: TemplateCategory, templateName: String, model: Any): String {
        val templatePath = "$category/$templateName"
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(freemarker.getTemplate(templatePath), model)
        } catch (e: Exception) {
            log.error("Exception occurred while processing template $templatePath", e.message)
        }
        return ""
    }
}