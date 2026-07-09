package com.template.app.data.model

data class User(
    val userId: String,
    val userName: String,
    val userPhone: String,
    val token: String,
    val avatar: String? = null
)
