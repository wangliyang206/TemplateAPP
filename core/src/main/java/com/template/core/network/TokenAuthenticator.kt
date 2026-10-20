package com.template.core.network

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okio.Buffer
import timber.log.Timber
import javax.inject.Inject

/**
 * 401 自动刷新：真正接进 OkHttpClient 的实现。
 *
 * 关键点（上一版缺失或写错的）：
 * 1. **token 在请求体里**，只重放原请求不会换 token，必须重写 body 的 token 字段；
 * 2. 并发 401 只允许刷新一次：拿锁后先比对「最新 token 是否已变化」，
 *    已变化说明别的线程刚刷过，直接用它重放；
 * 3. 最多重试一次，避免 401 死循环；
 * 4. 刷新失败 → 清理登录态并广播 [SessionManager.expired]。
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val refresher: TokenRefresher,
    private val sessionManager: SessionManager,
) : Authenticator {

    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorCount() >= MAX_RETRY) {
            Timber.w("TokenAuthenticator: 已达最大重试次数，放弃")
            return null
        }

        val failedToken = response.request.tokenFromBody().orEmpty()

        val newToken = synchronized(refreshLock) {
            val latest = runBlocking { tokenStore.currentToken() }
            when {
                latest.isEmpty() -> null
                // 其他线程已刷新，直接用最新 token 重放
                latest != failedToken -> latest
                else -> runBlocking { refresher.refresh(failedToken) }
                    ?.takeIf { it.isNotBlank() }
                    ?.also { runBlocking { tokenStore.updateToken(it) } }
            }
        }

        if (newToken == null) {
            Timber.w("TokenAuthenticator: 刷新失败，清理登录态")
            runBlocking { sessionManager.expire() }
            return null
        }
        if (newToken == failedToken) return null

        return response.request.rewriteToken(newToken) ?: response.request.newBuilder()
            .header(HEADER_AUTHORIZATION, "Bearer $newToken")
            .build()
    }

    /** 从统一请求体里读出 token */
    internal fun Request.tokenFromBody(): String? = runCatching {
        val body = body ?: return@runCatching null
        val buffer = Buffer()
        body.writeTo(buffer)
        JsonParser.parseString(buffer.readUtf8())
            .asJsonObject
            .get("token")
            ?.takeIf { !it.isJsonNull }
            ?.asString
    }.getOrNull()

    /** 重写请求体中的 token；非 JSON body 返回 null，调用方降级为只改 header */
    internal fun Request.rewriteToken(newToken: String): Request? = runCatching {
        val body = body ?: return@runCatching null
        val contentType = body.contentType()
        val buffer = Buffer()
        body.writeTo(buffer)
        val json = JsonParser.parseString(buffer.readUtf8()).asJsonObject
        json.addProperty("token", newToken)
        newBuilder()
            .header(HEADER_AUTHORIZATION, "Bearer $newToken")
            .method(method, json.toString().toRequestBody(contentType))
            .build()
    }.getOrNull()

    private fun Response.priorCount(): Int {
        var count = 0
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private const val MAX_RETRY = 1
        private const val HEADER_AUTHORIZATION = "Authorization"
    }
}
