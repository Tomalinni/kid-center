package com.joins.kidcenter.utils

import java.text.AttributedCharacterIterator
import java.text.FieldPosition
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

class SafeDateFormat : SimpleDateFormat {
    val formats: ThreadLocal<SimpleDateFormat>

    constructor(pattern: String) : super(pattern) {
        formats = ThreadLocal.withInitial { SimpleDateFormat(pattern) }
    }

    override fun parse(source: String?): Date {
        return formats.get().parse(source)
    }

    override fun parse(text: String, pos: ParsePosition): Date {
        return formats.get().parse(text, pos)
    }

    override fun parseObject(source: String?, pos: ParsePosition): Any {
        return formats.get().parseObject(source, pos)
    }

    override fun parseObject(source: String?): Any {
        return formats.get().parseObject(source)
    }

    override fun format(date: Date, toAppendTo: StringBuffer, pos: FieldPosition): StringBuffer {
        return formats.get().format(date, toAppendTo, pos)
    }

    override fun formatToCharacterIterator(obj: Any): AttributedCharacterIterator {
        return formats.get().formatToCharacterIterator(obj)
    }

}