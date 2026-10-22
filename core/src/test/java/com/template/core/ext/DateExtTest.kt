package com.template.core.ext

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

/**
 * 时间格式化的 JVM 测试。
 *
 * 时区固定成 UTC：`SimpleDateFormat` 跟随默认时区，
 * 不固定的话同一条断言在上海机器通过、在 CI（UTC）上失败。
 */
class DateExtTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `时间戳按指定 pattern 格式化`() {
        assertEquals("1970-01-01", 0L.toDateString(DatePattern.DATE))
        assertEquals("1970-01-01 00:00", 0L.toDateString())
        assertEquals("1970-01-01 00:00:00", 0L.toDateString(DatePattern.DATE_TIME_SECOND))
    }

    @Test
    fun `字符串时间戳解析失败返回空串而不抛异常`() {
        assertEquals("1970-01-01", "0".toDateString(DatePattern.DATE))
        assertEquals("", "not-a-number".toDateString())
        assertEquals("", "".toDateString())
    }

    @Test
    fun `相对时间按分钟小时天分级`() {
        val now = 1_700_000_000_000L
        assertEquals("刚刚", (now - 10_000L).toFriendlyTime(now))
        assertEquals("1 分钟前", (now - 90_000L).toFriendlyTime(now))
        assertEquals("1 小时前", (now - 3_600_000L).toFriendlyTime(now))
        assertEquals("2 天前", (now - 2 * 86_400_000L).toFriendlyTime(now))
    }

    @Test
    fun `超过 30 天回落到日期`() {
        val now = 1_700_000_000_000L
        val text = (now - 40L * 86_400_000L).toFriendlyTime(now)
        assertTrue("应为日期形态：$text", text.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")))
    }

    @Test
    fun `未来时间不会出现负数`() {
        val now = 1_700_000_000_000L
        assertEquals("刚刚", (now + 5_000L).toFriendlyTime(now))
    }

    @Test
    fun `isToday 只对当天为真`() {
        val now = 1_700_000_000_000L
        assertTrue(now.isToday(now))
        assertFalse((now - 2 * 86_400_000L).isToday(now))
    }
}
