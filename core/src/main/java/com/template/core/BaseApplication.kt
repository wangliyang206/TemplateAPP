package com.template.core

import android.app.Application
import com.tencent.mmkv.MMKV
import timber.log.Timber

open class BaseApplication : Application() {

    companion object {
        lateinit var INSTANCE: BaseApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        initTimber()
        MMKV.initialize(this)
        initGlobalExceptionHandler()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initGlobalExceptionHandler() {
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(
                com.template.core.exception.GlobalExceptionHandler(this)
            )
        }
    }
}
