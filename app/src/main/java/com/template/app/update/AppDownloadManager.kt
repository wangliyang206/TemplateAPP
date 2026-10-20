package com.template.app.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class AppDownloadManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    suspend fun download(context: Context, url: String, fileName: String): File? = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) file.delete()
        _state.value = DownloadState.Downloading(0)
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                _state.value = DownloadState.Failed("下载失败: HTTP ${response.code}")
                return@withContext null
            }
            val body = response.body ?: run {
                _state.value = DownloadState.Failed("响应为空")
                return@withContext null
            }
            val total = body.contentLength()
            val buffer = ByteArray(BUFFER_SIZE)
            var downloaded: Long = 0
            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        val progress = if (total > 0) (downloaded * 100 / total).toInt() else 0
                        _state.value = DownloadState.Downloading(progress)
                    }
                }
            }
            _state.value = DownloadState.Completed(file)
            file
        } catch (e: IOException) {
            _state.value = DownloadState.Failed(e.message ?: "下载异常")
            null
        }
    }

    fun reset() {
        _state.value = DownloadState.Idle
    }

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Int) : DownloadState()
        data class Completed(val file: File) : DownloadState()
        data class Failed(val message: String) : DownloadState()
    }

    private companion object {
        /** 8KB 是 Okio/文件流的常用块大小：太小系统调用多，太大占内存且进度回调不细腻 */
        const val BUFFER_SIZE = 8 * 1024
    }
}
