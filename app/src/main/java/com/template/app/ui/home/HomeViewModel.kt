package com.template.app.ui.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.template.app.data.model.Article
import com.template.app.ui.list.ArticlePagingSource
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 首页 ViewModel：使用 Paging3 展示文章列表
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    pagingSource: ArticlePagingSource
) : BaseViewModel() {

    val articleFlow: Flow<PagingData<Article>> =
        androidx.paging.Pager(
            config = androidx.paging.PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            )
        ) { pagingSource }
            .flow
            .cachedIn(viewModelScope)
}
