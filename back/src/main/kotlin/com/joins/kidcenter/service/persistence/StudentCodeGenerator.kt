package com.joins.kidcenter.service.persistence

import com.joins.kidcenter.repository.StudentRepository
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

interface StudentCodeGenerator {
    fun nextTrialId(): String

    fun nextRegularId(): String
}

@Service
open class StudentCodeGeneratorImpl
@Autowired constructor(val repository: StudentRepository,
                       val transformer: StudentCodeTransformer) : StudentCodeGenerator {


    override fun nextTrialId(): String {
        val id = repository.selectNextTrialId()
        return transformer.stringifyTrialId(id)
    }

    override fun nextRegularId(): String {
        val id = repository.selectNextRegularId()
        return transformer.stringifyRegularId(id)
    }
}

@Component
open class StudentCodeTransformer {
    companion object {
        private val businessIdRegex = Regex("\\w\\d{3}|\\d{3}\\w")
        private val businessIdPartialNumericRegex = Regex("\\d{1,3}")
        private val trialIdStartSuffix: Char = 'Z'
        private val trialIdPadLength: Int = 3

        private val regularIdStartPrefix: Char = 'A'
        private val regularIdPadLength: Int = 3

        private val letterSpan: Int = 'Z' - 'A' + 1
        private val digitsSpan: Int = 1000
        private val idSpan: Int = letterSpan * digitsSpan
        val maxId: Int = idSpan - 1
    }

    fun stringifyTrialId(id: Long): String {
        validateId(id)
        val letter: Char = (trialIdStartSuffix - (id / digitsSpan).toChar()).toChar()
        val remainder: Long = id % digitsSpan
        val remainderStr = StringUtils.leftPad("$remainder", trialIdPadLength, '0')

        return "$remainderStr$letter"
    }

    fun stringifyRegularId(id: Long): String {
        validateId(id)
        val letter: Char = regularIdStartPrefix + (id / digitsSpan).toInt()
        val remainder: Long = id % digitsSpan
        val remainderStr = StringUtils.leftPad("$remainder", regularIdPadLength, '0')

        return "$letter$remainderStr"
    }

    fun isLikeBusinessId(text: String): Boolean {
        return text.matches(businessIdPartialNumericRegex) || text.matches(businessIdRegex)
    }

    private fun validateId(id: Long) {
        Validate.isTrue(id >= 0, "Positive id value is expected")
        Validate.isTrue(id < idSpan, "Id value can not be more than $idSpan")
    }
}