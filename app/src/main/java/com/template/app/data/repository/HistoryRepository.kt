package com.template.app.data.repository

import com.template.app.data.mapper.toArticle
import com.template.app.data.mapper.toHistoryEntity
import com.template.app.data.model.Article
import com.template.core.db.HistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 浏览历史仓储。
 *
 * 上限策略放在数据层：[MAX_ENTRIES] 之外由 DAO 一条 SQL 裁掉，
 * UI 不需要关心"历史会不会无限涨"。
 */
@Singleton
class HistoryRepository @Inject constructor(private val dao: HistoryDao) {

    fun observeAll(): Flow<List<Article>> = dao.observeAll().map { list -> list.map { it.toArticle() } }

    /** 历史条数，供「我的」页显示 */
    fun observeCount(): Flow<Int> = dao.observeCount()

    /**
     * 记录一次浏览。
     *
     * 同一条文章反复打开只保留一行（主键 REPLACE），所以不用担心列表被重复项撑爆。
     */
    suspend fun record(article: Article, now: Long = System.currentTimeMillis()) {
        dao.insert(article.toHistoryEntity(now))
        dao.trimTo(MAX_ENTRIES)
    }

    suspend fun clear() = dao.clear()

    companion object {
        /** 历史保留条数：够用又不至于让首屏查询变慢 */
        const val MAX_ENTRIES = 200
    }
}
