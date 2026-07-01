package com.template.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .setLenient()
            .create()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val cacheDir = File(com.template.core.BaseApplication.INSTANCE.cacheDir, "http_cache")
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .cache(Cache(cacheDir, NetworkConfig.CACHE_SIZE))
            .addInterceptor(LoggingInterceptor())
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
