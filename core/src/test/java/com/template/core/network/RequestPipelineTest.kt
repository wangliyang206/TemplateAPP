package com.template.core.network

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 请求装配链路的端到端测试：`ApiRequestFactory`（token 注入）→ `HeaderInterceptor`（统一请求头）→ 真实发出。
 *
 * 这是模板里最容易"看起来对、其实没接上"的一段：
 * 拦截器漏加、header 字段名写错、token 没进请求体，单测不覆盖的话只有联调时才炸。
 */
class RequestPipelineTest {

    private lateinit var server: MockWebServer

    private val client = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .build()

    private class FakeTokenStore(private var token: String) : TokenStore {
        override suspend fun currentToken(): String = token
        override suspend fun updateToken(token: String) {
            this.token = token
        }
        override suspend fun clearToken() {
            token = ""
        }
    }

    private data class Payload(val id: Int, val name: String)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        // NetworkConfig 是全局单例，恢复默认值避免污染其他用例
        NetworkConfig.deviceType = "Android"
        NetworkConfig.deviceOs = ""
        NetworkConfig.clientId = ""
        NetworkConfig.channel = "official"
        NetworkConfig.language = "ZH"
        SignUtils.init("")
        server.shutdown()
    }

    private fun enqueueOk() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"code":0}"""))
    }

    private fun postJson(json: String): Request = Request.Builder()
        .url(server.url("/api/test"))
        .post(json.toRequestBody("application/json".toMediaType()))
        .build()

    @Test
    fun `统一请求头按 NetworkConfig 真实发出`() {
        NetworkConfig.deviceOs = "Android 14"
        NetworkConfig.clientId = "device-abc"
        NetworkConfig.channel = "huawei"
        NetworkConfig.language = "EN"
        enqueueOk()

        client.newCall(postJson("{}")).execute().close()

        val recorded = server.takeRequest()
        assertEquals("Android", recorded.getHeader("Device-Type"))
        assertEquals("Android 14", recorded.getHeader("Device-Os"))
        assertEquals("device-abc", recorded.getHeader("Client-Id"))
        assertEquals("huawei", recorded.getHeader("Channel"))
        assertEquals("EN", recorded.getHeader("Language"))
    }

    @Test
    fun `配置为空的请求头不会被发成空字符串`() {
        NetworkConfig.deviceOs = ""
        NetworkConfig.clientId = ""
        enqueueOk()

        client.newCall(postJson("{}")).execute().close()

        val recorded = server.takeRequest()
        // 空值不发送：服务端拿到 null 与拿到 "" 的处理常常不同
        assertNull(recorded.getHeader("Device-Os"))
        assertNull(recorded.getHeader("Client-Id"))
    }

    @Test
    fun `鉴权接口的 token 与签名会进入请求体`() = runTest {
        SignUtils.init("secret")
        enqueueOk()
        val factory = ApiRequestFactory(FakeTokenStore("token-1"))

        val request = factory.build(Payload(1, "demo"))
        val json = Gson().toJson(request)
        client.newCall(postJson(json)).execute().close()

        val body = server.takeRequest().body.readUtf8()
        assertTrue("请求体应包含 token：$body", body.contains("\"token\":\"token-1\""))
        assertTrue("请求体应包含非空 sign：$body", body.contains("\"sign\":\"") && !body.contains("\"sign\":\"\""))
        assertEquals("token-1", request.token)
    }

    @Test
    fun `匿名接口不携带 token`() = runTest {
        val factory = ApiRequestFactory(FakeTokenStore("token-1"))

        val request = factory.build(Payload(1, "demo"), authenticated = false)

        assertEquals("", request.token)
    }
}
