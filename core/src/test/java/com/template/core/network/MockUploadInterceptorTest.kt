package com.template.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * MockUploadInterceptor 的纯 JVM 测试。
 *
 * 注意：开关是 NetworkConfig（全局 object）上的字段，
 * 每个用例结束后必须恢复默认值，避免污染其他测试。
 */
class MockUploadInterceptorTest {

    private lateinit var server: MockWebServer
    private val client = OkHttpClient.Builder()
        .addInterceptor(MockUploadInterceptor())
        .build()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        NetworkConfig.mockUploadEnabled = false
        server.shutdown()
    }

    private fun call(path: String): Int {
        val body = "payload".toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url(server.url(path))
            .put(body)
            .build()
        return client.newCall(request).execute().use { it.code }
    }

    @Test
    fun `开关开启时演示端点被短路返回 200 且不落真实服务器`() {
        NetworkConfig.mockUploadEnabled = true

        assertEquals(200, call("/file/demo"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `开关关闭时演示端点原样放行`() {
        NetworkConfig.mockUploadEnabled = false
        server.enqueue(MockResponse().setResponseCode(404))

        assertEquals(404, call("/file/demo"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `开关开启时非演示端点仍放行`() {
        NetworkConfig.mockUploadEnabled = true
        server.enqueue(MockResponse().setResponseCode(204))

        assertEquals(204, call("/user/avatar"))
        assertEquals(1, server.requestCount)
    }
}
