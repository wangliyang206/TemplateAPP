package com.template.core.network

object NetworkConfig {

    // ========== 超时配置 ==========
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // ========== 统一请求头配置 ==========
    /** 设备类型 (Android / iOS) */
    var deviceType: String = "Android"

    /** 操作系统版本，如 "Android 14" */
    var deviceOs: String = ""

    /** 客户端唯一标识 (ANDROID_ID 或 UUID) */
    var clientId: String = ""

    /** 渠道来源 */
    var channel: String = "official"

    /** 语言偏好，如 "ZH"、"EN"、"AR" */
    var language: String = "ZH"

    // ========== 请求体配置 ==========
    /** API 版本号（整数） */
    var apiVersion: Int = 1

    /** 客户端设备信息对象（随请求体发送） */
    var clientInfo: ClientInfo = ClientInfo()

    // ========== 签名配置 ==========
    /** 签名密钥，从服务端获取 */
    var signSecret: String = ""
}
