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
 *     .authenticator(TokenAuthenticator(dataStoreManager))
 *     .addInterceptor(TokenInterceptor(dataStoreManager))
 *     .build()
 *
 * 说明：
 * - 检测到 401 时自动调用刷新接口获取新 token
 * - 刷新成功后使用新 token 重试原请求
 * - 刷新失败则清空本地 token，引导用户重新登录
 */
class TokenAuthenticator(
    private val dataStore: DataStoreManager,
    private val refreshTokenCall: suspend (token: String) -> RefreshResult?
) : Authenticator {

    private var isRefreshing = false
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) {
            return null // 不需要 token 的请求，不处理
        }

        synchronized(lock) {
            if (isRefreshing) {
                // 正在刷新中，用新 token 重试
                val newToken = runBlocking { dataStore.getUserSync().token }
                return if (newToken.isNotEmpty()) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else null
            }

            isRefreshing = true
            try {
                // 同步获取当前 token
                val currentToken = runBlocking { dataStore.getUserSync().token }
                if (currentToken.isEmpty()) {
                    isRefreshing = false
                    return null
                }

                // 调用刷新接口
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
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    // 刷新失败
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
