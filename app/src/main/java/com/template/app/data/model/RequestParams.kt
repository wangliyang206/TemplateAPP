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
