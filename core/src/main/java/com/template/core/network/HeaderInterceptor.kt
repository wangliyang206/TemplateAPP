package com.template.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 统一请求头。
 *
 * 字段值来自 [NetworkConfig]，由 Application 启动时通过 DeviceInfoCollector 填充。
 * （上一版这些 header 被整段注释掉，配置形同虚设。）
 */
class HeaderInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        NetworkConfig.deviceType.takeIf { it.isNotEmpty() }
            ?.let { builder.header("Device-Type", it) }
        NetworkConfig.deviceOs.takeIf { it.isNotEmpty() }
            ?.let { builder.header("Device-Os", it) }
        NetworkConfig.clientId.takeIf { it.isNotEmpty() }
            ?.let { builder.header("Client-Id", it) }
        NetworkConfig.channel.takeIf { it.isNotEmpty() }
            ?.let { builder.header("Channel", it) }
        NetworkConfig.language.takeIf { it.isNotEmpty() }
            ?.let { builder.header("Language", it) }

        return chain.proceed(builder.build())
    }
}
