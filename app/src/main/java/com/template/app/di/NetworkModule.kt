package com.template.app.di

import com.template.app.BuildConfig
import com.template.app.data.api.ApiService
import com.template.app.data.api.MockApiService
import com.template.app.data.model.RefreshTokenParams
import com.template.core.di.CoreNetworkModule
import com.template.core.network.ApiRequest
import com.template.core.network.TokenRefresher
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named(CoreNetworkModule.BASE_URL)
    fun provideBaseUrl(): String = BuildConfig.API_HOST

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = if (BuildConfig.IS_DEBUG_DATA) {
        Timber.d("使用 MockApiService（IS_DEBUG_DATA=true）")
        MockApiService()
    } else {
        Timber.d("使用真实 ApiService：${BuildConfig.API_HOST}")
        retrofit.create(ApiService::class.java)
    }

    /**
     * Token 刷新实现。
     *
     * 用 [Lazy] 打断依赖环：
     * ApiService → Retrofit → OkHttp → TokenAuthenticator → TokenRefresher → ApiService
     */
    @Provides
    @Singleton
    fun provideTokenRefresher(apiService: Lazy<ApiService>): TokenRefresher = TokenRefresher { oldToken ->
        runCatching {
            apiService.get()
                .refreshToken(ApiRequest.build(RefreshTokenParams(oldToken), token = oldToken))
                .data
                ?.token
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
