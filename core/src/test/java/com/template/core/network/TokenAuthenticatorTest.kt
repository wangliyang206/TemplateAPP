package com.template.core.network

import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Token 自动刷新的核心回归测试。
 *
 * 覆盖上一版的真实缺陷：token 在请求体里，重放原请求不会换 token。
 */
class TokenAuthenticatorTest {

    private class FakeTokenStore(var token: String) : TokenStore {
        var cleared = false
        override suspend fun currentToken(): String = token
        override suspend fun updateToken(token: String) {
            this.token = token
        }
        override suspend fun clearToken() {
            token = ""
            cleared = true
        }
    }

    private fun requestWithBody(token: String): Request = Request.Builder()
        .url("https://api.example.com/member/profile")
        .post("""{"version":1,"token":"$token","data":{}}""".toRequestBody())
        .build()

    private fun unauthorized(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("Unauthorized")
        .body("".toResponseBody(null))
        .build()

    @Test
    fun `401 时刷新成功并重写请求体中的 token`() = runTest {
        val store = FakeTokenStore("old-token")
        val manager = SessionManager(store)
        val authenticator = TokenAuthenticator(store, { _ -> "new-token" }, manager)

        val retried = authenticator.authenticate(null, unauthorized(requestWithBody("old-token")))

        assertTrue(retried != null)
        val body = retried!!.body!!
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        assertTrue(buffer.readUtf8().contains("\"token\":\"new-token\""))
        assertEquals("Bearer new-token", retried.header("Authorization"))
        assertEquals("new-token", store.token)
    }

    @Test
    fun `刷新失败时清理登录态并放弃重试`() = runTest {
        val store = FakeTokenStore("old-token")
        val manager = SessionManager(store)
        val authenticator = TokenAuthenticator(store, { _ -> null }, manager)

        val retried = authenticator.authenticate(null, unauthorized(requestWithBody("old-token")))

        assertNull(retried)
        assertTrue(store.cleared)
    }

    @Test
    fun `本地没有 token 时不发起刷新`() = runTest {
        val store = FakeTokenStore("")
        val manager = SessionManager(store)
        var refreshCalled = false
        val authenticator = TokenAuthenticator(
            store,
            { _ ->
                refreshCalled = true
                null
            },
            manager,
        )

        val retried = authenticator.authenticate(null, unauthorized(requestWithBody("")))

        assertNull(retried)
        assertTrue(!refreshCalled)
    }

    @Test
    fun `其他线程已刷新时直接复用最新 token 不重复刷新`() = runTest {
        val store = FakeTokenStore("already-refreshed")
        val manager = SessionManager(store)
        var refreshCalled = false
        val authenticator = TokenAuthenticator(
            store,
            { _ ->
                refreshCalled = true
                null
            },
            manager,
        )

        val retried = authenticator.authenticate(null, unauthorized(requestWithBody("old-token")))

        assertTrue(retried != null)
        assertTrue(!refreshCalled)
        assertEquals("Bearer already-refreshed", retried!!.header("Authorization"))
    }
}
