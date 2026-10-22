package com.template.app.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.template.app.data.model.Article
import com.template.app.data.repository.ArticleRepository
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 搜索页 ViewModel —— 「防抖 + 取消上一次请求」的参考实现。
 *
 * 三个运算符各司其职，缺一个都会出问题：
 * - [debounce]：输入过程中不请求，停 300ms 才发，避免每个字符打一次接口；
 * - [distinctUntilChanged]：删了又输回同样的词（或光标移动导致的重复回填）不重复请求；
 * - [flatMapLatest]：关键词一变就**取消**上一个 PagingSource 的请求，
 *   否则先发的慢请求后到，会把新关键词的结果覆盖掉（经典的搜索错乱）。
 *
 * PagingData 必须 [cachedIn]，否则旋转屏幕 / 返回栈回退时列表会被重新加载。
 */
@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: ArticleRepository) : BaseViewModel() {

    private val _keyword = MutableStateFlow("")
    val keyword: StateFlow<String> = _keyword.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: Flow<PagingData<Article>> = _keyword
        .map { it.trim() }
        .debounce(DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { word ->
            if (word.isEmpty()) flowOf(PagingData.empty()) else repository.search(word)
        }
        .cachedIn(viewModelScope)

    fun onKeywordChange(text: String) {
        _keyword.value = text
    }

    private companion object {
        /** 300ms 是手感与请求量的折中：再短会明显多打接口，再长会觉得"卡" */
        const val DEBOUNCE_MS = 300L
    }
}
