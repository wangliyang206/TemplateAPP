package com.template.core.network

import com.template.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor：自动为请求添加 Authorization Header
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

        val token = runBlocking { dataStore.userFlow.first().token }
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
