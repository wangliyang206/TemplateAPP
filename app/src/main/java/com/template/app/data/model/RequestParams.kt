package com.template.app.data.model

/**
 * 业务请求参数数据类
 *
 * 这些类用于填充 ApiRequest.data 字段
 */

data class LoginParams(
    val phone: String,
    val password: String
)

data class RegisterParams(
    val phone: String,
    val password: String
)

data class CheckUpdateParams(
    val versionCode: Int
)

data class ArticleListParams(
    val page: Int,
    val pageSize: Int
)
