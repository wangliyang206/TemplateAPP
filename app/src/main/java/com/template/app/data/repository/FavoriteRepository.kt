package com.template.app.data.repository

import com.template.app.data.mapper.toArticle
import com.template.app.data.mapper.toFavoriteEntity
import com.template.app.data.model.Article
import com.template.core.db.FavoriteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 收藏仓储。
 *
 * 没有网络请求，Room 的 Flow 就是真相源：写库后所有观察方（详情页按钮、收藏列表）
 * 自动收到更新，不需要手动通知或事件总线。
 */
@Singleton
class FavoriteRepository @Inject constructor(private val dao: FavoriteDao) {

    fun observeAll(): Flow<List<Article>> = dao.observeAll().map { list -> list.map { it.toArticle() } }

    fun observeIsFavorite(articleId: Long): Flow<Boolean> = dao.observeIsFavorite(articleId)

    /** 收藏总数，供「我的」页显示 */
    fun observeCount(): Flow<Int> = dao.observeCount()

    /**
     * 切换收藏状态，返回切换**之后**的状态（true = 已收藏）。
     *
     * UI 直接拿返回值更新按钮，不用再查一次库。
     */
    suspend fun toggle(article: Article, now: Long = System.currentTimeMillis()): Boolean {
        val alreadyFavorite = dao.isFavorite(article.id)
        return if (alreadyFavorite) {
            dao.deleteById(article.id)
            false
        } else {
            dao.insert(article.toFavoriteEntity(now))
            true
        }
    }
}
