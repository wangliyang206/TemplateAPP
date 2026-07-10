package com.template.app.di

import com.template.app.BuildConfig
import com.template.app.data.api.ApiService
import com.template.app.data.api.MockApiService
import com.template.core.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        // 根据 BuildConfig.IS_DEBUG_DATA 决定使用真实 API 还是 Mock 数据
        return if (BuildConfig.IS_DEBUG_DATA) {
            Timber.d("Using MockApiService for debug data")
            MockApiService()
        } else {
            Timber.d("Using real ApiService")
            RetrofitClient.create()
        }
    }
}
