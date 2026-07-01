package com.template.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .setLenient()
            .create()
    }

    private var _retrofit: Retrofit? = null

    fun init(baseUrl: String) {
        if (_retrofit != null) return
        Timber.d("RetrofitClient init with baseUrl: $baseUrl")
        val client = OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(LoggingInterceptor())
            .build()

        _retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val retrofit: Retrofit
        get() = _retrofit ?: throw IllegalStateException("RetrofitClient.init() must be called in Application.onCreate()")

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
