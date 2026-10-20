package com.template.core

import android.app.Application
import android.os.StrictMode
import timber.log.Timber

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initStrictMode()
        initTimber()
        initGlobalExceptionHandler()
    }

    /**
     * 仅 debug 生效的主线程 I/O / 资源泄漏检测。
     * 违规只打日志不崩溃，提前暴露「磁盘读写放在主线程」「Activity/Closeable 泄漏」这类问题，
     * release 构建完全无此逻辑，不产生任何开销。
     */
    private fun initStrictMode() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build(),
        )
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initGlobalExceptionHandler() {
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(
                com.template.core.exception.GlobalExceptionHandler(this),
            )
        }
    }
}
