package com.template.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor：为每个请求添加统一的请求头
 *
 * 规范要求：
 * - Device-Type: 设备类型 (Android/iOS)
 * - Device-Os: 操作系统版本
 * - Client-Id: 客户端唯一标识
 * - Channel: 渠道来源
 * - Language: 语言偏好
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
//            .header("Device-Type", NetworkConfig.deviceType)
//            .header("Device-Os", NetworkConfig.deviceOs)
//            .header("Client-Id", NetworkConfig.clientId)
//            .header("Channel", NetworkConfig.channel)
//            .header("Language", NetworkConfig.language)
            .build()
        return chain.proceed(request)
    }
}
