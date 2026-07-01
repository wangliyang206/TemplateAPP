package com.template.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.template.app.BuildConfig
import com.template.app.data.model.UpdateInfo
import java.io.File

object UpdateChecker {

    fun shouldUpdate(remote: UpdateInfo): Boolean {
        return remote.versionCode > BuildConfig.VERSION_CODE
    }

    fun installApk(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                @Suppress("DEPRECATION")
                Uri.fromFile(file)
            }
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
