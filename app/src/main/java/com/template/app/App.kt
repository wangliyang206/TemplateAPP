package com.template.app

import com.template.core.BaseApplication
import com.template.core.network.RetrofitClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(BuildConfig.API_HOST)
    }
}
