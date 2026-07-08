package com.template.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okio.buffer
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 文件上传管理器 — 带进度回调
 *
 * 用法：
 * val manager = FileUploadManager()
 * val result = manager.upload(
 *     url = "https://api.example.com/upload",
 *     file = File("/sdcard/photo.jpg"),
 * }) { progress ->
 *     // 更新进度 UI (0-100)
 *     binding.progressBar.progress = progress
 * }
 */
object FileUploadManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * 单文件上传（带进度）
     * @param url 上传接口地址
     * @param file 要上传的文件
     * @param headers 额外请求头
     * @param onProgress 进度回调 (0-100)
     */
    suspend fun upload(
        url: String,
        file: File,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Int) -> Unit)? = null
    ): Result<String> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val progressBody = if (onProgress != null) {
                ProgressRequestBody(requestBody, onProgress)
            } else requestBody

            val request = Request.Builder()
                .url(url)
                .put(progressBody)
                .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(body)
            } else {
                Result.failure(Exception("Upload failed: HTTP ${response.code} $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 多文件上传（Multipart）
     */
    suspend fun uploadMulti(
        url: String,
        files: List<File>,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Int) -> Unit)? = null
    ): Result<String> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            files.forEachIndexed { index, file ->
                builder.addFormDataPart(
                    "file_$index",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
            }
            val totalSize = builder.build().contentLength()

            val request = Request.Builder()
                .url(url)
                .post(builder.build())
                .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(body)
            } else {
                Result.failure(Exception("Upload failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 带进度回调的 RequestBody
 */
internal class ProgressRequestBody(
    private val delegate: okhttp3.RequestBody,
    private val onProgress: (Int) -> Unit
) : okhttp3.RequestBody() {

    override fun contentType() = delegate.contentType()
    override fun contentLength() = delegate.contentLength()
    override fun writeTo(sink: okio.BufferedSink) {
        val total = contentLength()
        val countingSink = object : okio.ForwardingSink(sink) {
            var bytesWritten = 0L
            override fun write(source: okio.Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                if (total > 0) {
                    val progress = (bytesWritten * 100 / total).toInt()
                    onProgress(progress)
                }
            }
        }
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}
