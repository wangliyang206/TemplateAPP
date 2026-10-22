package com.template.core.ext

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 时间格式化。
 *
 * 注意：本项目 minSdk 24 且未开启 core library desugaring，因此**不要**用 `java.time`，
 * 统一走 [SimpleDateFormat]。需要时区/夏令时极端正确的场景，再考虑开脱糖。
 * 常用格式见 [DatePattern]。
 */
private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS
private const val FRIENDLY_DAY_LIMIT = 30

/** 毫秒时间戳 → 指定格式字符串。例：`System.currentTimeMillis().toDateString(DatePattern.DATE)` */
fun Long.toDateString(pattern: String = DatePattern.DATE_TIME): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))

/** 字符串时间戳（后端常见的秒级/毫秒级）→ 指定格式；解析失败返回空串而不是抛异常。 */
fun String.toDateString(pattern: String = DatePattern.DATE_TIME): String =
    toLongOrNull()?.toDateString(pattern).orEmpty()

/**
 * 相对时间：刚刚 / x 分钟前 / x 小时前 / x 天前，超过 30 天回落到日期。
 * 例：`(now - 90_000).toFriendlyTime()` → `1 分钟前`
 */
fun Long.toFriendlyTime(now: Long = System.currentTimeMillis()): String {
    val diff = (now - this).coerceAtLeast(0)
    return when {
        diff < MINUTE_MS -> "刚刚"
        diff < HOUR_MS -> "${diff / MINUTE_MS} 分钟前"
        diff < DAY_MS -> "${diff / HOUR_MS} 小时前"
        diff < FRIENDLY_DAY_LIMIT * DAY_MS -> "${diff / DAY_MS} 天前"
        else -> toDateString(DatePattern.DATE)
    }
}

/** 是否为今天（用于列表里区分"今天 10:20"与"10-19"）。 */
fun Long.isToday(now: Long = System.currentTimeMillis()): Boolean =
    toDateString(DatePattern.DATE) == now.toDateString(DatePattern.DATE)
