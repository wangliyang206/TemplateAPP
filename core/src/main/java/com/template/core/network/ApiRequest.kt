package com.template.core.network

/**
 * 统一请求体包装模型
 *
 * 接口规范要求每个请求必须包装为：
 * {
 *   "token": "xxx",
 *   "version": "v1",
 *   "client": "App",
 *   "sign": "签名值",
 *   "data": { ... 业务参数 ... }
 * }
 */
data class ApiRequest<T>(
    val token: String = "",
    val version: String = "",
    val client: String = "",
    val sign: String = "",
    val data: T
) {
    companion object {
        fun <T : Any> build(data: T, token: String = ""): ApiRequest<T> {
            return ApiRequest(
                token = token,
                version = NetworkConfig.apiVersion,
                client = NetworkConfig.clientName,
                sign = SignUtils.signData(data),
                data = data
            )
        }
    }
}
