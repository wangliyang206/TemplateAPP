package com.template.core.ext

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * 数字格式化的 JVM 测试。
 *
 * 固定 Locale 是为了让"千分位"这类依赖默认区域的输出可断言——
 * 否则同一份代码在不同区域机器上会得到 1,234 与 1.234 两种结果。
 */
class NumberExtTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `金额默认保留两位并补零`() {
        assertEquals("12.50", 12.5.toMoney())
        assertEquals("0.00", 0.0.toMoney())
        assertEquals("1234.50", 1234.5.toMoney())
    }

    @Test
    fun `金额可指定小数位`() {
        assertEquals("12.5", 12.5.toMoney(digits = 1))
    }

    @Test
    fun `整数千分位`() {
        assertEquals("1,234,567", 1234567L.toThousands())
        assertEquals("0", 0L.toThousands())
        assertEquals("999", 999L.toThousands())
    }

    @Test
    fun `文件大小按 1024 进位`() {
        assertEquals("0 B", 0L.toFileSize())
        assertEquals("1023 B", 1023L.toFileSize())
        assertEquals("1.0 KB", 1024L.toFileSize())
        assertEquals("1.5 KB", 1536L.toFileSize())
        assertEquals("1.0 MB", (1024L * 1024).toFileSize())
        assertEquals("1.0 GB", (1024L * 1024 * 1024).toFileSize())
    }

    @Test
    fun `负数的文件大小兜底为 0 B`() {
        assertEquals("0 B", (-1L).toFileSize())
    }

    @Test
    fun `百分比按 0 到 1 的小数换算`() {
        assertEquals("45.6%", 0.456.toPercent())
        assertEquals("100.0%", 1.0.toPercent())
        assertEquals("0.0%", 0.0.toPercent())
    }
}
