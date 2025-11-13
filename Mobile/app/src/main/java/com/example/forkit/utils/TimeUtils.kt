package com.example.forkit.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object TimeUtils {

    private val isoPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    fun parseIsoToMillis(value: String?, fallback: Long = System.currentTimeMillis()): Long {
        if (value.isNullOrBlank()) return fallback
        for (pattern in isoPatterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val date = formatter.parse(value)
                if (date != null) {
                    return date.time
                }
            } catch (_: ParseException) {
                // try the next pattern
            } catch (_: IllegalArgumentException) {
                // try the next pattern
            }
        }
        return fallback
    }
}


