package com.template.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.template.app.data.api.ApiService
import com.template.app.data.model.Article
import com.template.app.data.paging.ArticlePagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文章列表仓储。
 *
 * Pager 的组装放在数据层（而不是 ViewModel）：
 * 分页参数属于数据策略，ViewModel 只应消费 Flow。
 */
@Singleton
class ArticleRepository @Inject constructor(private val apiService: ApiService) {

    fun articles(categoryId: Int): Flow<PagingData<Article>> = pager(ArticlePagingSource(apiService, categoryId))

    /**
     * 关键词搜索：复用同一个 [ArticlePagingSource]，只是多带一个 keyword 参数。
     *
     * 搜索页的"取消上一次请求"由 ViewModel 的 flatMapLatest 保证（换关键词时上游 PagingSource 被取消），
     * 数据层不需要额外做什么。
     */
    fun search(keyword: String): Flow<PagingData<Article>> = pager(ArticlePagingSource(apiService, keyword = keyword))

    private fun pager(source: ArticlePagingSource): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = 3,
        ),
    ) { source }.flow

    private companion object {
        const val PAGE_SIZE = 20
    }
}
