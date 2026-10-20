package com.template.app.data.repository

import android.content.Context
import com.template.core.di.CoreNetworkModule
import com.template.core.network.FileUploadManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 文件上传仓储 —— [FileUploadManager] 的 app 侧调用示例。
 *
 * 分层与其它 Repository 一致：ViewModel 只调用挂起函数并消费进度，
 * URL 拼接、演示文件的生成这类脏活留在数据层。
 *
 * 真实业务里把 [uploadDemoFile] 换成业务接口（如头像 PUT /user/avatar、
 * 附件 POST multipart），写法不变。
 */
@Singleton
class UploadRepository @Inject constructor(
    private val uploader: FileUploadManager,
    @ApplicationContext private val context: Context,
    @Named(CoreNetworkModule.BASE_URL) private val baseUrl: String,
) {

    /**
     * 演示：在 cacheDir 生成一个小文本文件并带进度上传。
     *
     * 上传端点 `$baseUrl{PATH_DEMO}`：IS_DEBUG_DATA 构建下由 MockUploadInterceptor
     * 短路返回伪 200（见 NetworkConfig.mockUploadEnabled），示例可跑通成功分支；
     * 接入真实后端时替换端点并关闭 mock 开关。
     * 用 Result 返回而不是抛异常：上传失败属于「业务可预期的结果」，
     * 由调用方决定展示方式。
     */
    suspend fun uploadDemoFile(onProgress: (Int) -> Unit): Result<String> {
        val demoFile = withContext(Dispatchers.IO) { createDemoFile() }
        return uploader.upload(
            url = "$baseUrl$PATH_DEMO",
            file = demoFile,
            onProgress = onProgress,
        )
    }

    private fun createDemoFile(): File = File(context.cacheDir, "demo_upload.txt").apply {
        writeText("TemplateAPP 上传示例文件\n生成时间: ${System.currentTimeMillis()}\n")
    }

    private companion object {
        const val PATH_DEMO = "file/demo"
    }
}
