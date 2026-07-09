package com.template.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.template.app.data.api.ApiService
import com.template.app.data.model.Article
import com.template.app.data.model.ArticleListParams
import com.template.core.network.ApiRequest
import com.template.core.network.NetworkConfig
import com.template.core.network.SignUtils
import javax.inject.Inject

/**
 * PagingSource：为 Paging3 提供分页数据
 */
class ArticlePagingSource @Inject constructor(
    private val apiService: ApiService
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val requestParams = ArticleListParams(page, params.loadSize)
            val request = ApiRequest(
                token = "",
                version = NetworkConfig.apiVersion,
                client = NetworkConfig.clientName,
                sign = SignUtils.signData(requestParams),
                data = requestParams
            )
            val response = apiService.getArticleList(request)
            val data = response.data?.list ?: emptyList()
            LoadResult.Page(
                data = data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }
}
