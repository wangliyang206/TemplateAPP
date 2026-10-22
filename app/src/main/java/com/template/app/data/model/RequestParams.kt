package com.template.app.data.model

/**
 * 业务请求参数数据类
 *
 * 这些类用于填充 ApiRequest.data 字段
 */

data class LoginParams(val mobile: String, val password: String)

data class RegisterParams(val mobile: String, val password: String)

data class RefreshTokenParams(val token: String)

data class CheckUpdateParams(val versionCode: Int)

// keyword 是搜索页用到的可选参数：留空时后端按普通列表返回，行为不变
data class ArticleListParams(val page: Int, val pageSize: Int, val categoryId: Int = 0, val keyword: String = "")

data class BannerListParams(val position: Int = 0)

data class CategoryListParams(val type: Int = 0)

/** 排行不需要分页，只按条数取 */
data class RankListParams(val size: Int = 10)

/** 消息列表按类型过滤：null 表示全部 */
data class MessageListParams(val type: MessageType? = null)

/** 文章详情 */
data class ArticleDetailParams(val articleId: Long)
