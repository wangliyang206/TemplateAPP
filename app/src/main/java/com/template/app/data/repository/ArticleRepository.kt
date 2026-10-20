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

    fun articles(categoryId: Int): Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = 3,
        ),
    ) {
        ArticlePagingSource(apiService, categoryId)
    }.flow

    private companion object {
        const val PAGE_SIZE = 20
    }
}
