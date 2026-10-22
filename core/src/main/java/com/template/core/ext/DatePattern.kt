package com.template.core.ext

/**
 * 常用时间格式。
 *
 * 单独成文件是为了同时满足两件事：格式常量集中可查、`DateExt` 里不再出现
 * "一个 object + 若干扩展函数"导致的 detekt MatchingDeclarationName 告警。
 */
object DatePattern {
    const val DATE = "yyyy-MM-dd"
    const val DATE_TIME = "yyyy-MM-dd HH:mm"
    const val DATE_TIME_SECOND = "yyyy-MM-dd HH:mm:ss"
    const val TIME = "HH:mm"
    const val MONTH_DAY = "MM-dd"
}
