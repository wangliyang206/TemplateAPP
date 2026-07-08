package com.template.core.network

import com.template.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

/**
 * OkHttp Authenticator：401 自动刷新 Token
 *
 * 用法：
 * val client = OkHttpClient.Builder()
 *     .authenticator(TokenAuthenticator(dataStoreManager, refreshBlock))
 *     .addInterceptor(TokenInterceptor(dataStoreManager))
 *     .build()
 *
 * 说明：
 * - 检测到 401 时自动调用刷新接口获取新 token
 * - 刷新成功后使用新 token 重试原请求
 * - 刷新失败则清空本地 token，引导用户重新登录
 *
 * 注意：新规范中 token 在请求体 ApiRequest 中。本 Authenticator 对任意 401 响应
 * 都会尝试刷新本地 token（若本地无 token 则跳过）。
 */
class TokenAuthenticator(
    private val dataStore: DataStoreManager,
    private val refreshTokenCall: suspend (token: String) -> RefreshResult?
) : Authenticator {

    private var isRefreshing = false
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(lock) {
            if (isRefreshing) {
                return response.request
            }

            isRefreshing = true
            try {
                val currentToken = runBlocking { dataStore.getUserSync().token }
                if (currentToken.isEmpty()) {
                    isRefreshing = false
                    return null
                }

                val result = runBlocking { refreshTokenCall(currentToken) }
                return if (result != null) {
                    val newToken = result.token
                    runBlocking {
                        dataStore.saveUser(
                            userId = dataStore.getUserSync().userId,
                            token = newToken,
                            userName = dataStore.getUserSync().userName
                        )
                    }
                    isRefreshing = false
                    // 重试原请求（token 在 body 中，Repository 重建 ApiRequest 时自动取最新 token）
                    response.request
                } else {
                    runBlocking { dataStore.clearUser() }
                    isRefreshing = false
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed")
                isRefreshing = false
                return null
            }
        }
    }
}

data class RefreshResult(
    val token: String,
    val expireTime: Long = 0
)
