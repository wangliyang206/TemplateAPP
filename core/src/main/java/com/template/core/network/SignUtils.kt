package com.template.core.network

import com.google.gson.Gson
import java.security.MessageDigest

/**
 * 签名工具：生成防篡改 sign 字段
 *
 * 签名算法：
 * 1. 将 data 序列化为 JSON 字符串
 * 2. 拼接 secretKey
 * 3. MD5 哈希
 */
object SignUtils {

    private var secretKey: String = ""
    private val gson = Gson()

    fun init(secret: String) {
        secretKey = secret
    }

    /**
     * 对业务数据生成签名
     */
    fun signData(data: Any): String {
        if (secretKey.isEmpty()) return ""
        val json = gson.toJson(data)
        val raw = "$json&key=$secretKey"
        return md5(raw)
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
