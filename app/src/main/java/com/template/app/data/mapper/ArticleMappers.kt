package com.template.app.data.mapper

import com.template.app.data.model.Article
import com.template.core.db.FavoriteEntity
import com.template.core.db.HistoryEntity

/**
 * 文章 → 本地实体 的映射。
 *
 * 说明：实体里**不存 cover**。列表/详情都用 `WebViewActivity` 或纯文字条目，
 * 不展示封面图，所以没必要为它占一列、也没必要为它写迁移。
 * 若之后收藏页要显示封面，加列 + 写迁移即可（`AppDatabase` 里有模板）。
 */

fun Article.toFavoriteEntity(savedAt: Long): FavoriteEntity = FavoriteEntity(
    articleId = id,
    title = title,
    desc = desc,
    url = url,
    author = author,
    createTime = createTime,
    savedAt = savedAt,
)

fun FavoriteEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    desc = desc,
    url = url,
    author = author,
    createTime = createTime,
)

fun Article.toHistoryEntity(viewedAt: Long): HistoryEntity = HistoryEntity(
    articleId = id,
    title = title,
    desc = desc,
    url = url,
    author = author,
    createTime = createTime,
    viewedAt = viewedAt,
)

fun HistoryEntity.toArticle(): Article = Article(
    id = articleId,
    title = title,
    desc = desc,
    url = url,
    author = author,
    createTime = createTime,
)
