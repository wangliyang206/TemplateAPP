package com.template.app.di

import android.content.Context
import com.template.core.datastore.DataStoreManager
import com.template.core.datastore.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager = DataStoreManager(context)

    /** 仓储层依赖 [SessionStore] 接口而不是具体类，单测里用内存假实现替换 */
    @Provides
    @Singleton
    fun provideSessionStore(impl: DataStoreManager): SessionStore = impl
}
