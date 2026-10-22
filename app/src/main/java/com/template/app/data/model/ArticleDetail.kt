package com.template.app.data.model

/**
 * 文章详情。
 *
 * [content] 是正文的纯文本段落（模板里由 Mock 生成）。
 * 真实项目通常是 HTML，套 WebView 或富文本渲染——这里保持纯文本，
 * 是为了让「原生详情页」的示范聚焦在状态管理与交互上，不被富文本解析带偏。
 */
data class ArticleDetail(
    val article: Article,
    val content: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
)
