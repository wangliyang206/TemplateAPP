package com.template.core.state

import android.content.Context
import androidx.annotation.StringRes

/**
 * 面向 UI 的文案载体。
 *
 * 为什么不直接用 String：
 * - 网络层/领域层不应该拼装中文文案，否则无法国际化；
 * - 资源型文案只在真正渲染时用 Context 解析。
 */
sealed interface UiMessage {

    /** 服务端下发的文案，直接用 */
    data class Text(val value: String) : UiMessage

    /** 本地资源文案，支持占位符参数 */
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiMessage
}

fun UiMessage.asString(context: Context): String = when (this) {
    is UiMessage.Text -> value
    is UiMessage.Res -> if (args.isEmpty()) {
        context.getString(id)
    } else {
        context.getString(id, *args.toTypedArray())
    }
}

/** 便捷构造，允许 `UiMessage.of(R.string.xxx)` */
fun of(@StringRes id: Int, vararg args: Any): UiMessage = UiMessage.Res(id, args.toList())
