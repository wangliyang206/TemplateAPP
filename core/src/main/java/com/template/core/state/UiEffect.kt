package com.template.core.state

/**
 * 一次性副作用（导航、Toast、权限请求）。
 *
 * 绝对不能放进 [UiState]：配置变更后重放 State 会导致重复 Toast / 重复跳转。
 */
sealed interface UiEffect {

    data class ShowMessage(val message: UiMessage) : UiEffect

    /** 登录态失效，需要全局回到登录页 */
    data object SessionExpired : UiEffect

    /** 关闭当前页面 */
    data object Close : UiEffect
}
