package com.template.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
}

class ApiException(
    val code: Int = 0,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
