package com.template.core.exception

import android.content.Context
import android.os.Process
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
        } catch (_: Exception) {
        }
    }
}
