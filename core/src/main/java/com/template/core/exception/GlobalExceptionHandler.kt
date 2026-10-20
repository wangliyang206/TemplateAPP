package com.template.core.exception

import android.content.Context
import android.os.Process
import android.util.Log
import com.template.core.BuildConfig
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlobalExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        saveCrashLog(throwable)
        if (BuildConfig.DEBUG) {
            defaultHandler?.uncaughtException(thread, throwable)
        } else {
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val dir = File(context.filesDir, "crash")
            if (!dir.exists()) dir.mkdirs()
            val time = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val file = File(dir, "crash_$time.txt")
            FileWriter(file).use { writer ->
                PrintWriter(writer).use { pw ->
                    throwable.printStackTrace(pw)
                }
            }
        } catch (e: Exception) {
            // 崩溃落盘失败时不能再抛，否则会递归崩溃；但必须留痕，否则线上排查会彻底失明。
            Log.e(TAG, "崩溃日志写入失败", e)
        }
    }

    private companion object {
        const val TAG = "GlobalExceptionHandler"
    }
}
