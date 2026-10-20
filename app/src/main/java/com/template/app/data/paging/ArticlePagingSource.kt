package com.template.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.template.app.data.api.ApiService
import com.template.app.data.model.Article
import com.template.app.data.model.ArticleListParams
import com.template.core.network.ApiRequest
import javax.inject.Inject

/**
 * PagingSource：为 Paging3 提供分页数据
 *
 * @param apiService API 服务实例
 * @param categoryId 分类 ID，0 表示全部
 */
class ArticlePagingSource @Inject constructor(private val apiService: ApiService, private val categoryId: Int = 0) :
    PagingSource<Int, Article>() {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val requestParams = ArticleListParams(page, params.loadSize, categoryId)
            val request = ApiRequest.build(requestParams)
            val response = apiService.getArticleList(request)
            val data = response.data?.list ?: emptyList()
            LoadResult.Page(
                data = data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            // Paging3 的约定：网络/解析异常必须转成 LoadResult.Error 交给 UI，
            // 让它渲染"加载失败 + 重试"；这里若只捕获 IOException，
            // 服务端返回结构异常（JSON 解析失败）会直接把 App 打崩。
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? = state.anchorPosition?.let { pos ->
        state.closestPageToPosition(pos)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
    }
}
