package com.template.app.data.model

data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)
