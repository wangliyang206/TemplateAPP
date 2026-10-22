package com.template.app.ui.discover

import com.template.app.data.api.ApiService
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.app.data.model.RankArticle
import com.template.app.data.model.RankListParams
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

/**
 * 发现页数据源：分类 + 排行两个接口。
 *
 * 两个请求彼此独立，ViewModel 侧用 async 并行发起（见 [DiscoverViewModel]），
 * 这里保持单一职责：一个方法只发一个请求。
 */
class DiscoverRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
) : BaseRepository() {

    suspend fun loadCategories(): List<Category> = apiCall {
        safeApi { apiService.getCategoryList(requestFactory.build(CategoryListParams(type = 0))) }
    }

    suspend fun loadRanks(): List<RankArticle> = apiCall {
        safeApi { apiService.getRankList(requestFactory.build(RankListParams(size = RANK_SIZE))) }
    }

    private companion object {
        const val RANK_SIZE = 10
    }
}
