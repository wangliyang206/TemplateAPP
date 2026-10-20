package com.template.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.template.app.data.model.Article
import com.template.app.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * 分类文章列表 ViewModel。
 *
 * 说明：分页场景不适合套 BaseViewModel 的 UiState 约定
 * （分页自己的 LoadState 就表达了 加载中/错误/空），所以这里直接组合数据层 Flow。
 * 这也正是「基类只给能力、不强制状态形状」的原因。
 */
@HiltViewModel
class ArticleListViewModel @Inject constructor(
    repository: ArticleRepository
) : ViewModel() {

    private val categoryId = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val articleFlow: Flow<PagingData<Article>> = categoryId
        .flatMapLatest { repository.articles(it) }
        .cachedIn(viewModelScope)

    fun setCategoryId(id: Int) {
        if (categoryId.value != id) {
            categoryId.value = id
        }
    }
}
