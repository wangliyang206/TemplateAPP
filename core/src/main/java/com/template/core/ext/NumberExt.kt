package com.template.core.ext

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * 金额 / 数字格式化。
 *
 * 为什么不直接用 `String.format("%.2f")`：它跟随默认 Locale，在某些地区会输出逗号小数点，
 * 也可能丢精度。展示金额统一走 [toMoney]，需要千分位用 [toThousands]。
 */

private const val MAX_MONEY_DIGITS = 8
private const val MAX_PERCENT_DIGITS = 4
private const val BYTES_PER_UNIT = 1024L

/** 金额：保留两位小数（默认），不足补零。例：`12.5.toMoney()` → `12.50` */
fun Double.toMoney(digits: Int = 2): String =
    DecimalFormat("0.${"0".repeat(digits.coerceIn(1, MAX_MONEY_DIGITS))}").format(this)

/** 整数千分位。例：`1234567.toThousands()` → `1,234,567` */
fun Long.toThousands(): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(this)

/**
 * 文件大小，保留一位小数，单位自动升到 TB。
 * 例：`1536L.toFileSize()` → `1.5 KB`
 */
fun Long.toFileSize(): String {
    if (this < 0) return "0 B"
    if (this < BYTES_PER_UNIT) return "$this B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = this.toDouble() / BYTES_PER_UNIT
    var index = 0
    while (value >= BYTES_PER_UNIT && index < units.lastIndex) {
        value /= BYTES_PER_UNIT
        index++
    }
    return "${DecimalFormat("0.0").format(value)} ${units[index]}"
}

/** 百分比，入参是 0~1 的小数。例：`0.456.toPercent()` → `45.6%` */
fun Double.toPercent(digits: Int = 1): String =
    "${DecimalFormat("0.${"0".repeat(digits.coerceIn(1, MAX_PERCENT_DIGITS))}").format(this * 100)}%"
