package com.template.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件上传：带进度回调的单文件 / 多文件上传。
 *
 * **关键设计**：复用全局 [OkHttpClient]，而不是自己 new 一个。
 * 自己 new 的 client 不会挂 [TokenInterceptor]，上传接口会在登录后被判 401；
 * 复用全局 client 还能共享连接池与线程池，多文件并发时更省资源。
 *
 * 用法（Hilt 注入后）：
 * ```
 * class XxxRepository @Inject constructor(private val uploader: FileUploadManager) {
 *     suspend fun uploadAvatar(file: File) = uploader.upload(url, file) { progress ->
 *         // 0-100
 *     }
 * }
 * ```
 */
@Singleton
class FileUploadManager @Inject constructor(private val client: OkHttpClient) {

    /**
     * 单文件上传（带进度）。
     *
     * @param onProgress 进度回调 (0-100)，在 IO 线程触发；更新 UI 请自行切主线程
     */
    suspend fun upload(
        url: String,
        file: File,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Int) -> Unit)? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val body = file.asRequestBody(OCTET_STREAM)
            val requestBody = if (onProgress != null) ProgressRequestBody(body, onProgress) else body
            execute(
                Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
                    .build(),
            )
        }
    }

    /**
     * 多文件上传（Multipart，可选整体进度）。
     *
     * @param onProgress 所有文件合计的上传进度 (0-100)，null 表示不计算
     */
    suspend fun uploadMulti(
        url: String,
        files: List<File>,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Int) -> Unit)? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            files.forEachIndexed { index, file ->
                builder.addFormDataPart("file_$index", file.name, file.asRequestBody(OCTET_STREAM))
            }
            val multipart = builder.build()
            val requestBody: RequestBody =
                if (onProgress != null) ProgressRequestBody(multipart, onProgress) else multipart

            execute(
                Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
                    .build(),
            )
        }
    }

    private fun execute(request: Request): String {
        // 上传比普通接口慢，单独放宽写超时；newBuilder 复用连接池，不额外占资源
        val uploadClient = client.newBuilder()
            .writeTimeout(UPLOAD_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        uploadClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("上传失败: HTTP ${response.code} $body")
            }
            return body
        }
    }

    private companion object {
        val OCTET_STREAM = "application/octet-stream".toMediaTypeOrNull()
        const val UPLOAD_WRITE_TIMEOUT_SECONDS = 120L
    }
}

/**
 * 带进度回调的 [RequestBody]。
 *
 * 注意 [contentLength] 为 -1（分块传输）时无法计算百分比，此时不回调，
 * 调用方需自行处理"进度条不动"的情况。
 */
internal class ProgressRequestBody(private val delegate: RequestBody, private val onProgress: (Int) -> Unit) :
    RequestBody() {

    override fun contentType() = delegate.contentType()

    override fun contentLength() = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val total = contentLength()
        val countingSink = object : ForwardingSink(sink) {
            var bytesWritten = 0L

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                if (total > 0) {
                    onProgress((bytesWritten * PERCENT_MAX / total).toInt().coerceIn(0, PERCENT_MAX))
                }
            }
        }
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private companion object {
        const val PERCENT_MAX = 100
    }
}
