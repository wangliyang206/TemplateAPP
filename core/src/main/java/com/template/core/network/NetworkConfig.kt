package com.template.core.network

import com.template.core.BuildConfig

object NetworkConfig {
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val CACHE_SIZE = 10 * 1024 * 1024L

    val BASE_URL: String
        get() = BuildConfig.API_HOST
}
