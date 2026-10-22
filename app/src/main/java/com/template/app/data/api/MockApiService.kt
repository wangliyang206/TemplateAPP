// Mock 服务是"假数据表"：里面的尺寸/分页数字不是业务常量，抽成常量反而让假数据更难读，
// 故在文件级抑制 MagicNumber 并写明原因。
@file:Suppress("MagicNumber")

package com.template.app.data.api

import com.template.app.data.model.Article
import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.LoginParams
import com.template.app.data.model.RefreshTokenParams
import com.template.app.data.model.RegisterParams
import com.template.app.data.model.UpdateInfo
import com.template.app.data.model.User
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
    private fun <T> successResponse(data: T): BaseResponse<T> = BaseResponse(
        version = 1,
        errorInfo = null,
        data = data,
    )

    /**
     * 构建失败响应
     */
    private fun <T> errorResponse(code: Int, message: String): BaseResponse<T> = BaseResponse(
        version = 1,
        errorInfo = ErrorInfo(code, message),
        data = null,
    )

    // ===== 用户模块 =====

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
                    avatar = "https://picsum.photos/200/200?random=1",
                ),
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
                avatar = null,
            ),
        )
    }

    /**
     * 模拟刷新 Token
     */
    override suspend fun refreshToken(request: ApiRequest<RefreshTokenParams>): BaseResponse<User> {
        mockDelay()

        val oldToken = request.data.token
        if (oldToken.isBlank()) {
            return errorResponse(-401, "Token 无效，请重新登录")
        }
        return successResponse(
            User(
                userId = "mock_user_001",
                userName = "测试用户",
                userPhone = "15032134297",
                token = "mock_token_refreshed_${System.currentTimeMillis()}",
                avatar = "https://picsum.photos/200/200?random=1",
            ),
        )
    }

    // ===== 系统模块 =====

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
                updateLog = "1. 新增文章收藏功能\n2. 优化首页加载速度\n3. 修复已知Bug",
            ),
        )
    }

    // ===== 首页模块 =====

    /**
     * 模拟获取轮播广告列表 - 6 张图片
     */
    override suspend fun getBannerList(request: ApiRequest<BannerListParams>): BaseResponse<List<Banner>> {
        mockDelay()

        val banners = listOf(
            Banner(1, "新品推荐", "https://picsum.photos/800/400?random=101", "https://www.baidu.com"),
            Banner(2, "限时特惠", "https://picsum.photos/800/400?random=102", "https://github.com"),
            Banner(3, "热门活动", "https://picsum.photos/800/400?random=103", "https://developer.android.com"),
            Banner(4, "精选好物", "https://picsum.photos/800/400?random=104", "https://kotlinlang.org"),
            Banner(5, "品牌盛典", "https://picsum.photos/800/400?random=105", "https://www.csdn.net"),
            Banner(6, "会员专享", "https://picsum.photos/800/400?random=106", "https://www.jianshu.com"),
        )
        return successResponse(banners)
    }

    /**
     * 模拟获取分类列表 - 4 个分类
     */
    override suspend fun getCategoryList(request: ApiRequest<CategoryListParams>): BaseResponse<List<Category>> {
        mockDelay()

        val categories = listOf(
            Category(1, "推荐"),
            Category(2, "技术"),
            Category(3, "摄影"),
            Category(4, "资讯"),
        )
        return successResponse(categories)
    }

    // ===== 文章列表 =====

    /**
     * 分类对应的标题前缀
     */
    private val categoryTitles = mapOf(
        1 to listOf(
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
        ),
        2 to listOf(
            "Kotlin 协程异常处理详解",
            "Android 性能优化实战",
            "Jetpack Compose 动画教程",
            "Gradle 构建优化技巧",
            "Android 权限适配方案",
            "Compose 与 View 混合开发",
            "ViewModel 最佳实践",
            "LiveData 与 StateFlow 对比",
            "Room 数据库迁移指南",
            "WorkManager 高级用法",
        ),
        3 to listOf("精美风景图片", "城市夜景摄影", "自然风光大片", "建筑摄影精选", "人像摄影佳作", "美食摄影集锦", "动物世界图片", "科技产品展示", "艺术设计作品", "旅行日记配图"),
        4 to listOf(
            "科技前沿资讯",
            "互联网行业动态",
            "开发者大会回顾",
            "开源项目推荐",
            "技术趋势分析",
            "产品设计灵感",
            "创业故事分享",
            "职场经验交流",
            "行业报告解读",
            "未来科技展望",
        ),
    )

    /**
     * 分类对应的描述
     */
    private val categoryDescs = mapOf(
        1 to listOf(
            "深入讲解 Android Jetpack 组件的使用方法，帮助你快速掌握现代 Android UI 开发。",
            "掌握 Kotlin 协程的核心原理，学习如何优雅地处理异步操作。",
            "详细介绍 Room 数据库的使用技巧，包括查询优化和事务处理。",
            "从零开始封装网络请求框架，实现统一的错误处理和拦截。",
            "全面解析 MVVM 架构模式，理解各层之间的协作关系。",
        ),
        2 to listOf(
            "学习 Kotlin 协程的异常处理机制，避免应用崩溃。",
            "分析 App 性能瓶颈，提供实用的优化方案。",
            "探索 Jetpack Compose 的动画系统，打造流畅的动效。",
            "优化 Gradle 构建速度，提升开发效率。",
            "处理 Android 运行时权限，提供良好的用户体验。",
        ),
        3 to listOf(
            "这是一张精美的风景照片，展现大自然的壮丽景色。",
            "繁华都市的夜景，灯火辉煌美不胜收。",
            "自然风光无限好，山水之间显真情。",
            "现代建筑与艺术的完美结合。",
            "人物肖像摄影，捕捉精彩瞬间。",
        ),
        4 to listOf("了解最新科技动态，把握行业发展趋势。", "互联网行业最新资讯，助你拓展视野。", "各大开发者大会精彩内容回顾。", "精选优质开源项目，提升开发效率。", "深度分析技术发展趋势。"),
    )

    /**
     * 分类对应的链接
     */
    private val categoryUrls = mapOf(
        1 to listOf(
            "https://developer.android.google.cn/jetpack/compose", // Android Jetpack Compose 入门指南
            "https://www.kotlincn.net/docs/reference/coroutines-overview.html", // Kotlin Coroutines 实战技巧
            "https://developer.android.google.cn/training/data-storage/room", // Room 数据库最佳实践
            "https://square.github.io/retrofit/", // Retrofit + OkHttp 网络请求封装（GitHub Pages 国内可访问）
            "https://developer.android.google.cn/jetpack/guide", // MVVM 架构模式详解
        ),
        2 to listOf(
            "https://www.kotlincn.net/docs/reference/coroutines/exception-handling.html", // Kotlin 协程异常处理详解
            "https://developer.android.google.cn/topic/performance", // Android 性能优化实战
            "https://developer.android.google.cn/jetpack/compose/animation", // Jetpack Compose 动画教程
            "https://docs.gradle.org/current/userguide/performance.html", // Gradle 构建优化技巧（Gradle 文档国内可访问）
            "https://developer.android.google.cn/training/permissions/requesting", // Android 权限适配方案
        ),
        3 to listOf(
            "https://picsum.photos/1200/800?random=landscape-1", // 精美风景图片
            "https://picsum.photos/1200/800?random=citynight-2", // 城市夜景摄影
            "https://picsum.photos/1200/800?random=nature-3", // 自然风光大片
            "https://picsum.photos/1200/800?random=architecture-4", // 建筑摄影精选
            "https://picsum.photos/1200/800?random=portrait-5", // 人像摄影佳作
        ),
        4 to listOf(
            "https://www.36kr.com/", // 科技前沿资讯
            "https://www.huxiu.com/", // 互联网行业动态
            "https://www.geekpark.net/", // 开发者大会回顾
            "https://gitee.com/explore", // 开源项目推荐（Gitee 国内版）
            "https://sspai.com/tag/Technology", // 技术趋势分析（少数派）
        ),
    )

    /**
     * 作者名称集合
     */
    private val authors = listOf("Android 开发者", "Kotlin 爱好者", "技术达人", "程序员小明", "架构师老王")

    /** 每个分类生成的假数据条数 */
    private val mockArticleTotal = 50

    /**
     * 模拟获取文章列表 —— 支持分页、分类筛选与关键词搜索，每个分类 50 条数据
     */
    override suspend fun getArticleList(request: ApiRequest<ArticleListParams>): BaseResponse<ArticleListResponse> {
        mockDelay()

        val params = request.data
        val page = params.page
        val pageSize = params.pageSize
        val categoryId = params.categoryId.takeIf { it > 0 } ?: 1
        val keyword = params.keyword.trim()

        val titles = categoryTitles[categoryId] ?: categoryTitles.values.first()
        val descs = categoryDescs[categoryId] ?: categoryDescs.values.first()
        val urls = categoryUrls[categoryId] ?: categoryUrls.values.first()

        val baseId = categoryId.toLong() * 10000
        val all = (0 until mockArticleTotal).map { index ->
            Article(
                id = baseId + index + 1,
                title = titles[index % titles.size] + if (index >= titles.size) "（${index + 1}）" else "",
                desc = descs[index % descs.size],
                url = urls[index % urls.size],
                cover = "https://picsum.photos/400/300?random=${categoryId * 100 + index + 1}",
                author = authors[index % authors.size],
                createTime = "2024-0${(index % 12) + 1}-${(index % 28) + 1}",
            )
        }

        // 关键词只做大小写不敏感的包含匹配，够用来验证"输入 → 防抖 → 请求 → 渲染"这条链路
        val matched = if (keyword.isEmpty()) {
            all
        } else {
            all.filter { article ->
                article.title.contains(keyword, ignoreCase = true) ||
                    article.desc.contains(keyword, ignoreCase = true)
            }
        }

        val list = matched.drop((page - 1) * pageSize).take(pageSize)

        return successResponse(
            ArticleListResponse(
                total = matched.size,
                list = list,
            ),
        )
    }
}
