package com.example.a7_1p

import com.example.a7_1p.data.DateTimeFormatterUtil
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formattedDateTime_containsReadableParts() {
        val now = System.currentTimeMillis()

        val listFormatted = DateTimeFormatterUtil.formatForList(now)
        val detailFormatted = DateTimeFormatterUtil.formatForDetail(now)

        assertTrue(listFormatted.isNotBlank())
        assertTrue(detailFormatted.contains(" at "))
    }

    @Test
    fun newPostTimestamp_isRecent() {
        val createdAt = System.currentTimeMillis()
        val elapsedMillis = System.currentTimeMillis() - createdAt

        assertTrue("Timestamp should be close to current time", elapsedMillis in 0..2_000)
    }
}
