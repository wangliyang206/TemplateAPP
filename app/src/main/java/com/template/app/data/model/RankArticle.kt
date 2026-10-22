package com.template.app.data.model

/**
 * 热门排行条目。
 *
 * 复用 [Article] 而不是新造一个字段重复的类：排行本质上就是"文章 + 一个热度指标"，
 * 点击后同样跳详情页，复用同一个 Adapter 与跳转逻辑。
 */
data class RankArticle(val article: Article, val reads: Int)
