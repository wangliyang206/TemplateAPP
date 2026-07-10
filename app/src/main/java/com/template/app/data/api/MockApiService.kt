package com.template.app.data.api

import com.template.app.data.model.*
import com.template.core.config.CoreConfig
import com.template.core.network.ApiRequest
import com.template.core.network.BaseResponse
import com.template.core.network.ErrorInfo
import kotlinx.coroutines.delay

/**
 * Mock API 服务 - 用于本地测试数据
 *
 * 当 BuildConfig.IS_DEBUG_DATA 为 true 时，使用此类替代真实 API
 */
class MockApiService : ApiService {

    /**
     * 模拟网络延迟
     */
    private suspend fun mockDelay() {
        delay(CoreConfig.MOCK_DELAY_MS)
    }

    /**
     * 构建成功响应
     */
    private fun <T> successResponse(data: T): BaseResponse<T> {
        return BaseResponse(
            version = 1,
            errorInfo = null,
            data = data
        )
    }

    /**
     * 构建失败响应
     */
    private fun <T> errorResponse(code: Int, message: String): BaseResponse<T> {
        return BaseResponse(
            version = 1,
            errorInfo = ErrorInfo(code, message),
            data = null
        )
    }

    /**
     * 模拟登录
     */
    override suspend fun login(request: ApiRequest<LoginParams>): BaseResponse<User> {
        mockDelay()

        val params = request.data
        return if (params.mobile == "15032134297" && params.password == "123456") {
            // 登录成功
            successResponse(
                User(
                    userId = "mock_user_001",
                    userName = "测试用户",
                    userPhone = params.mobile,
                    token = "mock_token_${System.currentTimeMillis()}",
                    avatar = "https://picsum.photos/200/200?random=1"
                )
            )
        } else {
            // 登录失败 - 手机号或密码错误
            errorResponse(-1001, "手机号或密码错误")
        }
    }

    /**
     * 模拟注册
     */
    override suspend fun register(request: ApiRequest<RegisterParams>): BaseResponse<User> {
        mockDelay()

        val params = request.data
        // 模拟注册成功
        return successResponse(
            User(
                userId = "mock_user_${System.currentTimeMillis()}",
                userName = "新用户_${params.mobile.takeLast(4)}",
                userPhone = params.mobile,
                token = "mock_token_${System.currentTimeMillis()}",
                avatar = null
            )
        )
    }

    /**
     * 模拟检查更新
     */
    override suspend fun checkUpdate(request: ApiRequest<CheckUpdateParams>): BaseResponse<UpdateInfo> {
        mockDelay()

        // 模拟有新版本更新
        return successResponse(
            UpdateInfo(
                versionCode = 2,
                versionName = "1.1.0",
                downloadUrl = "https://example.com/app_v1.1.0.apk",
                isForce = false,
                apkSize = 15_000_000L, // 15MB
                updateLog = "1. 新增文章收藏功能\n2. 优化首页加载速度\n3. 修复已知Bug"
            )
        )
    }

    /**
     * 模拟获取文章列表
     */
    override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> {
        mockDelay()

        val params = request.data
        val page = params.page
        val pageSize = params.pageSize

        // 模拟分页数据
        val total = 50
        val totalPages = (total / pageSize) + 1

        val list = if (page <= totalPages) {
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, total)
            (startIndex until endIndex).map { index ->
                Article(
                    id = index.toLong() + 1,
                    title = "测试文章标题 ${index + 1}",
                    desc = "这是第 ${index + 1} 篇测试文章的描述内容，用于展示列表数据。",
                    url = "https://example.com/article/${index + 1}",
                    cover = "https://picsum.photos/400/300?random=${index + 1}",
                    author = "作者${(index % 5) + 1}",
                    createTime = "2024-01-${(index % 28) + 1}"
                )
            }
        } else {
            emptyList()
        }

        return successResponse(
            ArticleListResponse(
                total = total,
                list = list
            )
        )
    }
}