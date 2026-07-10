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
     * 文章标题集合 - 用于生成多样化的测试数据
     */
    private val titles = listOf(
        "Android Jetpack Compose 入门指南",
        "Kotlin Coroutines 实战技巧",
        "Room 数据库最佳实践",
        "Retrofit + OkHttp 网络请求封装",
        "MVVM 架构模式详解",
        "Paging3 分页加载实现",
        "Glide 图片加载优化",
        "DataStore 替代 SharedPreferences",
        "Hilt 依赖注入完全指南",
        "ConstraintLayout 布局进阶",
        "RecyclerView 性能优化",
        "ViewBinding 使用指南",
        "LiveData 与 Flow 对比",
        "WorkManager 后台任务管理",
        "Navigation Component 导航",
        "Material Design 组件库",
        "Kotlin 协程异常处理",
        "Android 权限适配",
        "App 启动优化方案",
        "内存泄漏检测与修复",
        "精美风景图片",
        "城市夜景摄影",
        "自然风光大片",
        "建筑摄影精选",
        "人像摄影佳作",
        "美食摄影集锦",
        "动物世界图片",
        "科技产品展示",
        "艺术设计作品",
        "旅行日记配图"
    )

    /**
     * 文章描述集合
     */
    private val descriptions = listOf(
        "深入讲解 Android Jetpack Compose 的核心概念和使用方法，帮助你快速掌握现代 Android UI 开发。",
        "掌握 Kotlin 协程的核心原理，学习如何在实际项目中优雅地处理异步操作。",
        "详细介绍 Room 数据库的使用技巧，包括查询优化、迁移策略和事务处理。",
        "从零开始封装 Retrofit + OkHttp 网络请求框架，实现统一的错误处理和请求拦截。",
        "全面解析 MVVM 架构模式，理解 ViewModel、Repository 和 LiveData 的协作关系。",
        "使用 Paging3 实现高效的分页加载，支持下拉刷新和上拉加载更多功能。",
        "优化 Glide 图片加载性能，包括缓存策略、图片压缩和内存管理。",
        "使用 DataStore 替代传统的 SharedPreferences，实现类型安全的数据持久化。",
        "学习 Hilt 依赖注入框架，简化 Android 应用的依赖管理。",
        "掌握 ConstraintLayout 的高级用法，构建复杂而灵活的布局结构。",
        "深入分析 RecyclerView 的性能瓶颈，提供实用的优化方案。",
        "告别 findViewById，使用 ViewBinding 实现类型安全的视图绑定。",
        "对比 LiveData 和 Kotlin Flow 的优缺点，选择适合项目的响应式编程方案。",
        "使用 WorkManager 管理后台任务，确保任务可靠执行。",
        "使用 Navigation Component 实现单 Activity 架构的页面导航。",
        "探索 Material Design 组件库，打造现代化的应用界面。",
        "学习 Kotlin 协程的异常处理机制，避免应用崩溃。",
        "处理 Android 6.0+ 的运行时权限，提供良好的用户体验。",
        "分析 App 启动流程，优化启动时间。",
        "使用 LeakCanary 检测内存泄漏，定位问题并修复。",
        "这是一张精美的风景照片，展现大自然的壮丽景色。",
        "繁华都市的夜景，灯火辉煌美不胜收。",
        "自然风光无限好，山水之间显真情。",
        "现代建筑与艺术的完美结合。",
        "人物肖像摄影，捕捉精彩瞬间。",
        "美食诱惑，让人垂涎三尺。",
        "动物世界的奇妙瞬间，感受生命的力量。",
        "科技产品展示，感受科技的魅力。",
        "艺术设计作品，创意无限。",
        "旅行日记配图，记录美好时光。"
    )

    /**
     * 文章链接集合 - 包含图片和网站链接
     */
    private val urls = listOf(
        "https://picsum.photos/800/600?random=1",
        "https://picsum.photos/800/600?random=2",
        "https://picsum.photos/800/600?random=3",
        "https://picsum.photos/800/600?random=4",
        "https://picsum.photos/800/600?random=5",
        "https://picsum.photos/800/600?random=6",
        "https://picsum.photos/800/600?random=7",
        "https://picsum.photos/800/600?random=8",
        "https://picsum.photos/800/600?random=9",
        "https://picsum.photos/800/600?random=10",
        "https://picsum.photos/800/600?random=11",
        "https://picsum.photos/800/600?random=12",
        "https://picsum.photos/800/600?random=13",
        "https://picsum.photos/800/600?random=14",
        "https://picsum.photos/800/600?random=15",
        "https://developer.android.com",
        "https://kotlinlang.org",
        "https://github.com",
        "https://www.baidu.com",
        "https://www.w3schools.com",
        "https://medium.com",
        "https://dev.to",
        "https://www.csdn.net",
        "https://www.jianshu.com",
        "https://www.oschina.net",
        "https://gradle.org",
        "https://www.androiddev.cn",
        "https://kotlin.github.io/kotlinx.coroutines/",
        "https://square.github.io/retrofit/",
        "https://bumptech.github.io/glide/"
    )

    /**
     * 作者名称集合
     */
    private val authors = listOf("Android 开发者", "Kotlin 爱好者", "技术达人", "程序员小明", "架构师老王")

    /**
     * 模拟获取文章列表 - 支持分页，共 200 条数据
     */
    override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> {
        mockDelay()

        val params = request.data
        val page = params.page
        val pageSize = params.pageSize

        // 总共 200 条数据，支持多页加载
        val total = 200
        val totalPages = (total + pageSize - 1) / pageSize

        val list = if (page <= totalPages) {
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, total)
            (startIndex until endIndex).map { index ->
                val titleIndex = index % titles.size
                val descIndex = index % descriptions.size
                val urlIndex = index % urls.size
                val authorIndex = index % authors.size
                val url = urls[urlIndex]

                Article(
                    id = index.toLong() + 1,
                    title = titles[titleIndex],
                    desc = descriptions[descIndex],
                    url = url,
                    cover = "https://picsum.photos/400/300?random=${index + 1}",
                    author = authors[authorIndex],
                    createTime = "2024-0${(index % 12) + 1}-${(index % 28) + 1}"
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