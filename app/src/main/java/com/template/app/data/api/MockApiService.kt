// Mock 服务是"假数据表"：里面的尺寸/分页数字不是业务常量，抽成常量反而让假数据更难读，
// 故在文件级抑制 MagicNumber 并写明原因。
@file:Suppress("MagicNumber")

package com.template.app.data.api

import com.template.app.data.model.Article
import com.template.app.data.model.ArticleDetail
import com.template.app.data.model.ArticleDetailParams
import com.template.app.data.model.ArticleListParams
import com.template.app.data.model.ArticleListResponse
import com.template.app.data.model.Banner
import com.template.app.data.model.BannerListParams
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.LoginParams
import com.template.app.data.model.Message
import com.template.app.data.model.MessageListParams
import com.template.app.data.model.MessageType
import com.template.app.data.model.RankArticle
import com.template.app.data.model.RankListParams
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
                    avatar = "res://avatar_default",
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
                avatar = "res://avatar_default",
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

        // 图片走本地资源（drawable-nodpi/），不依赖外网图床 —— Mock 演示不联网也能看到完整 UI。
        // 真实项目接后端后，这里换成服务器返回的 https URL 即可，UI 层代码零改动。
        val banners = listOf(
            Banner(1, "新品推荐", "res://banner_1", "https://www.baidu.com"),
            Banner(2, "限时特惠", "res://banner_2", "https://github.com"),
            Banner(3, "热门活动", "res://banner_3", "https://developer.android.google.cn"),
            Banner(4, "精选好物", "res://banner_4", "https://kotlinlang.org"),
            Banner(5, "品牌盛典", "res://banner_5", "https://www.csdn.net"),
            Banner(6, "会员专享", "res://banner_6", "https://www.jianshu.com"),
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
            "https://tuchong.com/", // 图虫：国内摄影社区
            "https://500px.com.cn/", // 500px 中国版：风光与人像摄影
            "https://www.xitek.com/", // 色影无忌：摄影器材与技巧
            "https://www.fengniao.com/", // 蜂鸟网：摄影教程与作品
            "https://sspai.com/tag/Photography", // 少数派摄影专题
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

        val all = buildArticles(categoryId)

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

    // ===== 发现模块 =====

    /**
     * 模拟热门排行 - 取分类 1 的前 10 条，热度按 [10_000, 9_800, ...] 递减。
     * 热度用固定序列而不是随机数：让列表顺序稳定，便于截图对比和回归验收。
     */
    override suspend fun getRankList(request: ApiRequest<RankListParams>): BaseResponse<List<RankArticle>> {
        mockDelay()

        val size = request.data.size.coerceIn(1, 20)
        // 排行条目复用列表的生成逻辑，只把 id 换成排行号段、再补一个热度值，
        // 这样详情页按 id 反查时标题/作者与排行里显示的完全一致。
        val base = buildArticles(1)
        val list = (0 until size).map { index ->
            RankArticle(
                article = base[index % base.size].copy(id = RANK_ID_BASE + index),
                reads = 12_800 - index * 640,
            )
        }
        return successResponse(list)
    }

    // ===== 消息模块 =====

    /**
     * 模拟站内消息。
     * 已读状态是**进程内**的：Mock 实例是单例（Hilt @Singleton），
     * 所以"点开后角标减少"在本次运行内有效，重启复位——够用来演示未读角标联动。
     */
    override suspend fun getMessageList(request: ApiRequest<MessageListParams>): BaseResponse<List<Message>> {
        mockDelay()
        val filter = request.data.type
        val list = mockMessages.map { it.copy() }
        return successResponse(if (filter == null) list else list.filter { it.type == filter })
    }

    private val mockMessages = listOf(
        Message(1, "v2.3.0 已发布", "新增收藏与浏览历史，支持离线查看已收藏内容。", "10 分钟前", MessageType.SYSTEM),
        Message(2, "系统维护通知", "本周日 02:00-04:00 进行服务升级，期间可能无法登录。", "2 小时前", MessageType.SYSTEM),
        Message(3, "Kotlin 爱好者 回复了你", "协程的取消传播这块讲得很清楚，收藏了。", "3 小时前", MessageType.INTERACT),
        Message(4, "程序员小明 赞了你的评论", "在《Room 数据库迁移实践》中", "5 小时前", MessageType.INTERACT),
        Message(5, "架构师老王 关注了你", "点击查看对方的主页与动态", "昨天", MessageType.INTERACT),
        Message(6, "新功能上线：搜索结果支持历史记录", "最近 10 条搜索词会自动保留，可一键清空。", "2 天前", MessageType.SYSTEM, isRead = true),
        Message(7, "技术达人 回复了你", "这个防抖写法我一直在用，确实比手写 Handler 稳。", "3 天前", MessageType.INTERACT, isRead = true),
        Message(8, "账号安全提醒", "检测到你的账号在新设备登录，如非本人操作请及时修改密码。", "5 天前", MessageType.SYSTEM, isRead = true),
    )

    /**
     * 按分类生成文章全集。
     *
     * 抽成独立函数是为了让列表、排行、详情三处共用同一份数据：
     * 否则详情页按 id 反查时，标题/作者很容易和列表对不上。
     */
    private fun buildArticles(categoryId: Int): List<Article> {
        val titles = categoryTitles[categoryId] ?: categoryTitles.values.first()
        val descs = categoryDescs[categoryId] ?: categoryDescs.values.first()
        val urls = categoryUrls[categoryId] ?: categoryUrls.values.first()
        val baseId = categoryId.toLong() * 10_000

        return (0 until mockArticleTotal).map { index ->
            Article(
                id = baseId + index + 1,
                title = titles[index % titles.size] + if (index >= titles.size) "（${index + 1}）" else "",
                desc = descs[index % descs.size],
                url = urls[index % urls.size],
                cover = "res://cover_placeholder",
                author = authors[index % authors.size],
                createTime = "2024-0${(index % 12) + 1}-${(index % 28) + 1}",
            )
        }
    }

    /**
     * 按 id 反查文章：列表来源的 id 形如 `分类 * 10000 + 序号`，
     * 排行来源的 id 是 `900000 + 序号`，两条规则分开处理。
     */
    private fun findArticle(articleId: Long): Article? {
        if (articleId >= RANK_ID_BASE) {
            val index = (articleId - RANK_ID_BASE).toInt()
            val ranks = buildArticles(1)
            return ranks.getOrNull(index)?.copy(id = articleId)
        }
        val categoryId = (articleId / 10_000).toInt().takeIf { it in categoryTitles.keys } ?: return null
        val index = (articleId % 10_000).toInt() - 1
        return buildArticles(categoryId).getOrNull(index)
    }

    override suspend fun getArticleDetail(request: ApiRequest<ArticleDetailParams>): BaseResponse<ArticleDetail> {
        mockDelay()
        val article = findArticle(request.data.articleId)
            ?: return errorResponse(-404, "文章不存在或已下架")
        return successResponse(
            ArticleDetail(
                article = article,
                content = buildContent(article),
                likeCount = 128 + (article.id % 40).toInt(),
                commentCount = 6 + (article.id % 12).toInt(),
            ),
        )
    }

    /**
     * 生成正文。
     * 内容与文章标题关联但不联网——够用来验证"详情页渲染 + 长文滚动 + 收藏按钮状态"这条链路。
     */
    private fun buildContent(article: Article): String = buildString {
        appendLine("这篇笔记围绕「${article.title}」展开，写给已经能独立写业务代码、开始在意边界情况的同学。")
        appendLine()
        appendLine("一、问题从哪来")
        appendLine("${article.desc}。这类问题在真实项目里很少以「报错」的形式出现，更多是偶发卡顿、数据不一致，或者只在某几台设备上复现。")
        appendLine()
        appendLine("二、先把现象拆成可验证的假设")
        appendLine("不要一上来就改代码。先写清楚「什么条件下必现」，再把它压缩成一个最小复现路径。能稳定复现，问题就解决了一半。")
        appendLine()
        appendLine("三、实现上的取舍")
        appendLine("同一件事通常有两三种写法。选哪一种，取决于你更怕什么：怕内存涨得快，就少缓存；怕请求太频繁，就加防抖；怕状态不同步，就把真相源收敛到一个地方。")
        appendLine()
        appendLine("四、怎么验证改对了")
        appendLine("给自己留三条验收：正常路径、异常路径、连续操作的边界。三条都过，再去点提交。")
    }

    private companion object {
        const val RANK_ID_BASE = 900_000L
    }
}
