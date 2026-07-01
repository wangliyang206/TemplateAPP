package com.template.core.network

object NetworkConfig {
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val CACHE_SIZE = 10 * 1024 * 1024L

    var BASE_URL: String = ""
        private set

    fun initialize(baseUrl: String) {
        BASE_URL = baseUrl
    }
}
