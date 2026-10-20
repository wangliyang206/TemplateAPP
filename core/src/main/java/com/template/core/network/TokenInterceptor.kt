package com.template.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 为需要鉴权的请求补上 Authorization 头。
 *
 * 本项目的 token 同时存在于请求体（ApiRequest.token，后端规范要求）与 header，
 * 两者由同一份 [TokenStore] 驱动，避免不一致。
 */
class TokenInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.isAuthExempt()) return chain.proceed(request)

        // OkHttp 拦截器运行在 IO 线程，这里阻塞读取是安全的
        val token = runBlocking { tokenStore.currentToken() }
        if (token.isEmpty()) return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build(),
        )
    }

    private fun okhttp3.Request.isAuthExempt(): Boolean {
        val path = url.encodedPath
        return AUTH_EXEMPT_PATHS.any { path.contains(it) }
    }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private val AUTH_EXEMPT_PATHS = listOf(
            "member/login",
            "member/register",
            "member/refreshToken",
        )
    }
}
