package com.template.core.network

import com.template.core.datastore.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor：自动为请求添加 Authorization Header（可选的额外认证层）
 *
 * 注意：新规范中 token 已在请求体 ApiRequest.token 中传递，
 * 本拦截器作为 HTTP 层面的辅助认证层（可选启用）。
 */
class TokenInterceptor(
    private val dataStore: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // 不需要 token 的接口路径白名单
        val skipPaths = listOf("/user/login", "/user/register")
        val path = original.url.encodedPath
        if (skipPaths.any { path.contains(it) }) {
            return chain.proceed(original)
        }

        val token = runBlocking { dataStore.getUserSync().token }
        val request = if (token.isNotEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
