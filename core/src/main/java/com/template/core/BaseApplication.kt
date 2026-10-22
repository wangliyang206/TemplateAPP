package com.template.core

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.strictmode.Violation
import timber.log.Timber
import java.util.concurrent.Executor

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
     *
     * API 28+ 额外做一件事：**过滤系统进程侧的违规**。
     * 起因：MIUI 在 startActivity 时会检查应用锁密码文件（`com.miui.server.AccessController`
     * → `File.exists`），这段读盘发生在 system_server，但 StrictMode 会把它归因到发起
     * Binder 调用的应用头上，日志里表现为「我们的 HomeFragment 触发了 DiskReadViolation」，
     * 实际与业务代码无关。不过滤的话这类噪音会把真正的业务违规淹掉。
     */
    private fun initStrictMode() {
        if (!BuildConfig.DEBUG) return

        val threadBuilder = StrictMode.ThreadPolicy.Builder().detectAll()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            threadBuilder.penaltyListener(VIOLATION_EXECUTOR) { violation ->
                if (!violation.isFromSystemProcess()) {
                    Timber.w("%s", violation.stackTraceToString())
                }
            }
        } else {
            // API 28 以下没有 penaltyListener，退回默认行为（可能带系统噪音，可接受）
            threadBuilder.penaltyLog()
        }
        StrictMode.setThreadPolicy(threadBuilder.build())

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .build(),
        )
    }

    /**
     * 判断违规是否发生在系统进程里。
     * 只看违规点自身的调用栈 —— 跨进程违规的 `# via Binder call` 调用方栈不在其中，
     * 因此不会误伤应用自己真实的违规。
     */
    private fun Violation.isFromSystemProcess(): Boolean = stackTrace.any { frame ->
        SYSTEM_PROCESS_PREFIXES.any { frame.className.startsWith(it) }
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

    private companion object {
        /** 违规回调需要直接跑在违规线程上，保持与 penaltyLog 一致的时序 */
        val VIOLATION_EXECUTOR = Executor { it.run() }

        /**
         * 系统进程侧类名前缀：这些栈帧下的 I/O 不是应用代码引起的。
         * 目前只收录实测确认过的两个；若在其他厂商 ROM 上遇到同类噪音，
         * 按其堆栈里的实际包名追加即可（形如 `com.xxx.server.`）。
         */
        val SYSTEM_PROCESS_PREFIXES = arrayOf(
            "com.miui.server.",
            "com.android.server.",
        )
    }
}
