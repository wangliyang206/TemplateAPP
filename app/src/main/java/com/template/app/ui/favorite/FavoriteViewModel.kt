package com.template.app.ui.favorite

import androidx.lifecycle.viewModelScope
import com.template.app.data.model.Article
import com.template.app.data.repository.FavoriteRepository
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 收藏列表 ViewModel。
 *
 * 与"点一次查一次"的页面不同，这里**持续观察** Room 的 Flow：
 * 在详情页取消收藏后返回，列表会自动少一条，不需要 `onResume` 重新加载。
 * 这是 Room + Flow 相比"手动刷新"最实际的收益。
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(private val repository: FavoriteRepository) : BaseViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { _articles.value = it }
        }
    }
}
