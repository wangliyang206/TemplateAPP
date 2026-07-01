package com.template.core.network

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorHandler {

    fun handle(e: Throwable): ApiException {
        return when (e) {
            is ApiException -> e
            is HttpException -> ApiException(e.code(), e.message(), e)
            is ConnectException, is UnknownHostException -> ApiException(-1, "网络连接失败，请检查网络", e)
            is SocketTimeoutException -> ApiException(-2, "网络请求超时", e)
            else -> ApiException(-3, e.message ?: "未知错误", e)
        }
    }
}
