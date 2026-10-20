package com.template.core.network

import com.google.gson.annotations.SerializedName

/**
 * 统一错误信息
 *
 * 接口规范：
 * - errorCode: 负数表示错误，正数表示警告，0 表示成功
 * - errorMessage: 错误描述
 *
 * 注意：服务端返回的 JSON key 为全小写（errorcode / errormessage），
 * 必须通过 @SerializedName 映射。
 */
data class ErrorInfo(
    @SerializedName("errorcode")
    val errorCode: Int,
    @SerializedName("errormessage")
    val errorMessage: String,
)

/**
 * 统一响应体
 *
 * 接口规范格式：
 * {
 *   "version": 1,
 *   "errorinfo": { "errorcode": 0, "errormessage": "..." } | null,
 *   "data": { ... 业务数据 ... } | null
 * }
 *
 * 成功时 errorinfo 为 null，失败时包含 errorcode + errormessage
 */
data class BaseResponse<T>(
    val version: Int,
    @SerializedName("errorinfo")
    val errorInfo: ErrorInfo?,
    val data: T?,
) {
    /** 判断响应是否成功 */
    val isSuccess: Boolean
        get() = errorInfo == null || errorInfo.errorCode == 0

    /** 获取错误码（无错误时返回 0） */
    val errorCode: Int
        get() = errorInfo?.errorCode ?: 0

    /** 获取错误信息（无错误时返回空字符串） */
    val errorMessage: String
        get() = errorInfo?.errorMessage ?: ""
}
