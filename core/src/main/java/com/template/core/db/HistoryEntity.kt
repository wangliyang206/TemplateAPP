package com.template.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 浏览历史。
 *
 * 与收藏共用同一套冗余字段（理由见 [FavoriteEntity]），主键同样是 articleId：
 * 同一条文章反复看只保留一行，更新 `viewedAt` 即可，靠 `OnConflictStrategy.REPLACE` 实现。
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val desc: String,
    val url: String,
    val author: String,
    val createTime: String,
    /** 最近一次浏览时间戳（毫秒），列表按此倒序 */
    val viewedAt: Long,
)
