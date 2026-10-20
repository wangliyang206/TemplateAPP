package com.template.core.state

/**
 * 页面数据的唯一状态契约。
 *
 * 全项目只允许这一种写法：
 *   Idle → Loading → Success / Empty / Error
 *
 * 说明：
 * - [Empty] 与 [Success] 是两种不同的 UI 表达（空态页 vs 内容页），必须区分；
 * - [Error] 携带 [UiMessage] 而不是字符串，保证可国际化；
 * - 一次性副作用（Toast、导航）不要塞进这里，用 UiEffect。
 */
sealed interface UiState<out T> {

    /** 初始态：还没发起过请求，用于区分「首次加载」与「加载完成但为空」 */
    data object Idle : UiState<Nothing>

    data object Loading : UiState<Nothing>

    data object Empty : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Error(val message: UiMessage) : UiState<Nothing> {

        /** 兼容直接传文案的场景，内部统一转为 [UiMessage.Text] */
        constructor(text: String) : this(UiMessage.Text(text))
    }
}
