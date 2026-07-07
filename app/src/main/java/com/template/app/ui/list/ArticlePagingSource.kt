package com.template.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.template.app.data.api.ApiService
import com.template.app.data.model.Article
import com.template.core.base.BaseRepository
import javax.inject.Inject

/**
 * PagingSource：为 Paging3 提供分页数据
 * 示例：文章列表分页
 */
class ArticlePagingSource @Inject constructor(
    private val apiService: ApiService
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getArticleList(page, params.loadSize)
            val data = response.data ?: emptyList()
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
