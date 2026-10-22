package com.template.core.ext

import android.content.Context
import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap

/**
 * View / 尺寸相关扩展。
 */

private const val DEFAULT_CLICK_INTERVAL_MS = 600L
private const val DP_ROUNDING = 0.5f

/** dp → px */
fun Context.dp2px(dp: Float): Int = (dp * resources.displayMetrics.density + DP_ROUNDING).toInt()

/** px → dp */
fun Context.px2dp(px: Float): Float = px / resources.displayMetrics.density

/**
 * 同一控件在 [interval] 毫秒内只响应第一次点击。
 *
 * 用途：防连点重复下单 / 重复跳转。比在业务里散落 `if (System.currentTimeMillis() - last < 600) return` 干净。
 *
 * ```kotlin
 * btnPay.setOnSingleClickListener { doPay() }
 * ```
 */
private val lastClickTimes = WeakHashMap<View, Long>()

fun View.setOnSingleClickListener(interval: Long = DEFAULT_CLICK_INTERVAL_MS, action: (View) -> Unit) {
    setOnClickListener { view ->
        val now = SystemClock.elapsedRealtime()
        val last = lastClickTimes[view] ?: 0L
        if (now - last >= interval) {
            lastClickTimes[view] = now
            action(view)
        }
    }
}

/** 用布尔值控制显隐，避免到处写 `if (x) VISIBLE else GONE`。 */
var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
