package com.template.app.data.model

/**
 * 用户数据模型
 */
data class User(
    val userId: String,
    val userName: String,
    val userPhone: String,
    val token: String,
    val avatar: String? = null
)
