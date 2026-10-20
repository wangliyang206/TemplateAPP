package com.template.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * 演示上传拦截器：仅当 [NetworkConfig.mockUploadEnabled] 为 true 时生效。
 *
 * 背景：dev 构建（IS_DEBUG_DATA=true）下 Retrofit 接口全部由 MockApiService 兜住，
 * 但 FileUploadManager 走原生 OkHttp，会真实打到 API_HOST；
 * 演示端点 `file/demo` 在真实后端不存在（404），上传示例必然失败。
 *
 * 本拦截器只拦「演示端点 + PUT/POST」，短路返回伪 200，让示例可以完整演示
 * 进度回调与成功分支；其余请求原样放行。接入真实上传接口后，
 * 把宿主侧演示端点换掉、关闭开关即可，无需删除本类。
 */
class MockUploadInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isDemoUpload = NetworkConfig.mockUploadEnabled &&
            request.method in UPLOAD_METHODS &&
            request.url.encodedPath.endsWith(DEMO_PATH)

        if (!isDemoUpload) return chain.proceed(request)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(HTTP_OK)
            .message("OK (mock)")
            .body(MOCK_BODY.toResponseBody(JSON_TYPE))
            .build()
    }

    private companion object {
        const val DEMO_PATH = "/file/demo"
        const val HTTP_OK = 200
        const val MOCK_BODY = """{"mock":true,"msg":"demo upload accepted"}"""
        val JSON_TYPE = "application/json".toMediaType()
        val UPLOAD_METHODS = setOf("PUT", "POST")
    }
}
