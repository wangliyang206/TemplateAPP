package com.template.core.network

/**
 * Token 刷新策略，由业务层实现（core 不感知具体刷新接口）。
 *
 * 返回 null 表示刷新失败，调用方会清理登录态并回到登录页。
 */
fun interface TokenRefresher {

    suspend fun refresh(oldToken: String): String?
}
