package com.template.app

import com.template.core.BaseApplication
import com.template.core.network.NetworkConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        NetworkConfig.initialize(BuildConfig.API_HOST)
    }
}
