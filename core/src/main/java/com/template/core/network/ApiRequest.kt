package com.template.core.network

/**
 * 客户端设备信息
 *
 * 接口规范要求 client 字段为一个包含设备信息的 Object
 */
data class ClientInfo(
    val cell: String = "",
    val deviceid: String = "",
    val simid: String = "",
    val os: String = "",
    val osver: String = "",
    val vercode: String = "",
    val vername: String = "",
    val ppilheight: String = "",
    val ppiwidth: String = ""
)

/**
 * 统一请求体包装模型
 *
 * 接口规范要求每个请求必须包装为：
 * {
 *   "userid": "用户ID",
 *   "version": 1,
 *   "client": { ... 设备信息 ... },
 *   "token": "用户令牌",
 *   "language": "ZH",
 *   "sign": "签名值",
 *   "data": { ... 业务参数 ... }
 * }
 */
data class ApiRequest<T>(
    val userid: String = "",
    val version: Int = 1,
    val client: ClientInfo = ClientInfo(),
    val token: String = "",
    val language: String = "ZH",
    val sign: String = "",
    val data: T
) {
    companion object {
        fun <T : Any> build(data: T, token: String = "", userId: String = ""): ApiRequest<T> {
            return ApiRequest(
                userid = userId,
                version = NetworkConfig.apiVersion,
                client = NetworkConfig.clientInfo,
                token = token,
                language = NetworkConfig.language,
                sign = SignUtils.signData(data),
                data = data
            )
        }
    }
}
