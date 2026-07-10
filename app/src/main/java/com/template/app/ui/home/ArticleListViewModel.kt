package com.template.app.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.template.app.data.api.ApiService
import com.template.app.data.model.Article
import com.template.app.data.paging.ArticlePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * 文章列表 ViewModel - 按分类加载文章数据
 */
@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    /**
     * 当前分类 ID
     */
    private val _categoryId = MutableStateFlow(0)

    /**
     * 文章列表数据流 - 根据分类 ID 切换
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val articleFlow: Flow<PagingData<Article>> = _categoryId
        .flatMapLatest { categoryId ->
            androidx.paging.Pager(
                config = androidx.paging.PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                )
            ) {
                ArticlePagingSource(apiService, categoryId)
            }
                .flow
                .cachedIn(viewModelScope)
        }

    /**
     * 设置分类 ID，触发新的分页加载
     */
    fun setCategoryId(categoryId: Int) {
        if (_categoryId.value != categoryId) {
            _categoryId.value = categoryId
        }
    }
}