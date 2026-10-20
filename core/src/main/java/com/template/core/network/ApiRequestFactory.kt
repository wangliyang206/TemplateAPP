package com.template.core.network

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一请求体工厂。
 *
 * 解决上一版的隐性问题：各 Repository 直接 `ApiRequest.build(params)`，
 * token 永远为空，鉴权只靠 header 兜底。
 * 现在 token 由 [TokenStore] 统一注入，鉴权接口与匿名接口用参数显式区分。
 */
@Singleton
class ApiRequestFactory @Inject constructor(
    private val tokenStore: TokenStore
) {

    suspend fun <T : Any> build(data: T, authenticated: Boolean = true): ApiRequest<T> {
        val token = if (authenticated) tokenStore.currentToken() else ""
        return ApiRequest.build(data, token = token)
    }
}
