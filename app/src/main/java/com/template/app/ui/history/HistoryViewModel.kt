package com.template.app.ui.history

import androidx.lifecycle.viewModelScope
import com.template.app.R
import com.template.app.data.model.Article
import com.template.app.data.repository.HistoryRepository
import com.template.core.base.BaseViewModel
import com.template.core.state.UiEffect
import com.template.core.state.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 浏览历史 ViewModel。
 *
 * 与收藏页同一套写法：`Room Flow` 是唯一真相源，`clear()` 之后不需要手动
 * 把列表置空 —— 数据库删除会推一条新数据过来，页面自动变成空态。
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(private val repository: HistoryRepository) : BaseViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { _articles.value = it }
        }
    }

    /** 清空由 UI 先弹确认框，这里只做执行 */
    fun clear() {
        launchCatching {
            repository.clear()
            sendEffect(UiEffect.ShowMessage(UiMessage.Res(R.string.history_cleared)))
        }
    }
}
