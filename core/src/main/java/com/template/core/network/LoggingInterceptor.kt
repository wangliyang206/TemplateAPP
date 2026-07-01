package com.template.core.network

import com.template.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class LoggingInterceptor : Interceptor {

    private val logger = HttpLoggingInterceptor.Logger { message ->
        // 项目中可使用 Timber.tag("OkHttp").d(message)
    }

    private val delegate = HttpLoggingInterceptor(logger).apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)
}
