package com.template.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.template.core.network.DataStoreTokenStore
import com.template.core.network.HeaderInterceptor
import com.template.core.network.LoggingInterceptor
import com.template.core.network.MockUploadInterceptor
import com.template.core.network.NetworkConfig
import com.template.core.network.TokenAuthenticator
import com.template.core.network.TokenInterceptor
import com.template.core.network.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * 网络层装配。
 *
 * 与上一版的区别：不再用 `RetrofitClient` 全局单例 + 手工 init()，
 * 改为 Hilt 提供，拦截器链在编译期可见，也便于在测试中替换。
 * BaseUrl 由业务层通过 [BASE_URL] 限定符提供（core 不感知 BuildConfig）。
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkModule {

    const val BASE_URL = "core_base_url"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .serializeNulls()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideTokenStore(impl: DataStoreTokenStore): TokenStore = impl

    @Provides
    @Singleton
    fun provideOkHttpClient(
        headerInterceptor: HeaderInterceptor,
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        // 顺序即语义：Mock短路 → Header → Token → Logging → Authenticator(401 刷新)
        .addInterceptor(MockUploadInterceptor())
        .addInterceptor(headerInterceptor)
        .addInterceptor(tokenInterceptor)
        .addInterceptor(LoggingInterceptor())
        .authenticator(tokenAuthenticator)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson, @Named(BASE_URL) baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}
