package com.template.app.data.model

/**
 * 轮播广告数据模型
 */
data class Banner(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val linkUrl: String = ""
)

/**
 * 分类数据模型
 */
data class Category(
    val id: Int,
    val name: String
)