package com.example.a7_1p.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormatterUtil {
    private val listFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a", Locale.getDefault())

    private val detailFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'at' h:mm:ss a z", Locale.getDefault())

    fun formatForList(createdAtMillis: Long): String {
        return Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(listFormatter)
    }

    fun formatForDetail(createdAtMillis: Long): String {
        return Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(detailFormatter)
    }
}
