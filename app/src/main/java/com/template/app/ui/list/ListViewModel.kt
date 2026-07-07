package com.template.app.ui.list

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.template.app.data.model.Article
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 列表页 ViewModel：使用 Paging3
 */
@HiltViewModel
class ListViewModel @Inject constructor(
    pagingSource: ArticlePagingSource
) : BaseViewModel() {

    /**
     * PagingData Flow，在 init 中构建，cachedIn 保证 ViewModel 重建不重新加载
     */
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
