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

package com.joins.kidcenter.db

import com.joins.kidcenter.db.DnTableNames.namesInClearOrder
import com.joins.kidcenter.repository.HelperRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Component
open class DbConfiguration {

    val defaultSchemaName = "public"

    var schemaName: String = defaultSchemaName
        @Value("\${spring.jpa.hibernate.default_schema:public}") private set
}

interface DbSequenceHelper {
    fun createSequenceIfNotExist(name: String)

    fun createRequiredSequences()

    fun resetSequence(name: String)

    fun resetCreatedSequences()

    fun clearTables()
}

@Service
@Transactional
open class DbSequenceHelperImpl @Autowired constructor(
        val repository: HelperRepository,
        val dbConfiguration: DbConfiguration,
        val em: EntityManager
) : DbSequenceHelper {
    override fun createSequenceIfNotExist(name: String) {
        if (!sequenceExists(name)) {
            createSequence(name)
        }
    }

    override fun createRequiredSequences() {
        DbSequenceNames.requiredSequences.forEach { createSequenceIfNotExist(it) }
    }

    override fun resetSequence(name: String) {
        if (sequenceExists(name)) {
            em.createNativeQuery("alter sequence $name restart").executeUpdate()
        }
    }

    private fun createSequence(name: String) {
        em.createNativeQuery("create sequence $name").executeUpdate()
    }

    private fun sequenceExists(name: String): Boolean {
        return repository.findSequencesCount(dbConfiguration.schemaName, name).toInt() > 0
    }

    override fun resetCreatedSequences() {
        val sequenceNames = repository.findSequences(dbConfiguration.schemaName)
        sequenceNames.forEach {
            resetSequence(it)
        }
    }

    override fun clearTables() {
        namesInClearOrder.forEach {
            em.createNativeQuery("delete from $it").executeUpdate()
        }
    }
}

object DbSequenceNames {
    const val regular_id = "regular_id_seq"
    const val trial_id = "trial_id_seq"

    val requiredSequences = arrayOf(regular_id, trial_id)
}

object DnTableNames {
    val namesInClearOrder = arrayOf(
            "templatelessonslot",
            "lessontemplate",
            "studentslot",
            "lessonslot",
            "promotiondetail",
            "promotionsource",
            "studentrelative_mobilenotifications",
            "studentrelative_emailnotifications",
            "student_studentrelative",
            "student_call",
            "school_visit",
            "studentcard",
            "studentrelativerole",
            "studentrelative",
            "student",
            "student_family",
            "teacher",
            "kindergarden",
            "homework",
            "card",

            "account_school",
            "school",
            "category",
            "account",
            "city",
            "payment",

            "approle_apppermission",
            "appuser_approle",
            "apppermission",
            "approle",
            "appuser"
    )
}