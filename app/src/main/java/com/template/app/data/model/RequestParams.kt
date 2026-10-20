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

data class ArticleListParams(val page: Int, val pageSize: Int, val categoryId: Int = 0)

data class BannerListParams(val position: Int = 0)

data class CategoryListParams(val type: Int = 0)
