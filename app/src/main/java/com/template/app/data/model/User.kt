package com.template.app.data.model

data class User(
    val userId: String,
    val userName: String,
    val phone: String,
    val token: String,
    val avatar: String? = null
)
