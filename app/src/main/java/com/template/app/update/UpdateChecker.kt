package com.template.app.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.template.app.BuildConfig
import com.template.app.data.model.UpdateInfo
import java.io.File

object UpdateChecker {

    fun shouldUpdate(remote: UpdateInfo): Boolean = remote.versionCode > BuildConfig.VERSION_CODE

    fun installApk(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // minSdk 24 已高于 FileProvider 的最低要求，无需再判断版本
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
