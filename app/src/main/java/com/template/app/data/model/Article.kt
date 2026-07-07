package com.template.app.data.model

/**
 * 文章数据模型 - 列表分页示例
 */
data class Article(
    val id: Long,
    val title: String,
    val desc: String,
    val cover: String = "",
    val author: String = "",
    val createTime: String = ""
)

/**
 * 文章列表响应
 */
data class ArticleListResponse(
    val total: Int,
    val list: List<Article>
)
