package com.template.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏文章。
 *
 * 为什么把标题/作者等字段**冗余存一份**而不是只存 articleId 再去网络拉：
 * 收藏页要能离线打开，且后端文章可能被删除/改标题——用户收藏的是"当时看到的那条"。
 * 这是内容类 App 的常规取舍，代价是列表字段变更时需要迁移（见 [AppDatabase.MIGRATIONS]）。
 */
@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val desc: String,
    val url: String,
    val author: String,
    val createTime: String,
    /** 收藏时间戳（毫秒），列表按此倒序 */
    val savedAt: Long,
)
