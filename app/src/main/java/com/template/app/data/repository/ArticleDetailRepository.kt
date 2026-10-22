package com.template.app.data.repository

import com.template.app.data.api.ApiService
import com.template.app.data.model.ArticleDetail
import com.template.app.data.model.ArticleDetailParams
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

/**
 * 文章详情仓储。
 *
 * 与 Paging 用的 [ArticleRepository] 分开：一个是分页流、一个是一次性请求，
 * 混在一起会让构造函数依赖变杂，也让"这个类是干什么的"变模糊。
 */
class ArticleDetailRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
) : BaseRepository() {

    suspend fun load(articleId: Long): ArticleDetail = apiCall {
        safeApi { apiService.getArticleDetail(requestFactory.build(ArticleDetailParams(articleId))) }
    }
}
