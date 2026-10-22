package com.template.app.ui.articledetail

import androidx.lifecycle.viewModelScope
import com.template.app.R
import com.template.app.data.model.ArticleDetail
import com.template.app.data.repository.ArticleDetailRepository
import com.template.app.data.repository.FavoriteRepository
import com.template.app.data.repository.HistoryRepository
import com.template.core.base.BaseViewModel
import com.template.core.state.UiEffect
import com.template.core.state.UiMessage
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 文章详情 ViewModel。
 *
 * 两条数据流刻意分开：
 * - [state]：网络详情（标题/正文），一次性请求，四态由 `launchWithState` 管；
 * - [isFavorite]：本地收藏状态，**观察 Room 的 Flow**。
 *
 * 为什么不合成一个 state：收藏是本地即时操作，不该被网络请求的 Loading/Error 连带影响；
 * 而且从「我的收藏」取消收藏返回本页时，按钮要自动变——观察 Flow 天然做到，不用回传结果。
 */
@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val detailRepository: ArticleDetailRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<ArticleDetail>>(UiState.Idle)
    val state: StateFlow<UiState<ArticleDetail>> = _state.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var articleId: Long = 0L

    /** 由 Activity 在 initView 调用；同一 id 且已有数据时不重复请求（配置变更后重建的场景） */
    fun load(id: Long) {
        if (id == 0L) {
            _state.value = UiState.Error(UiMessage.Res(R.string.article_detail_not_found))
            return
        }
        if (id == articleId && _state.value is UiState.Success) return
        articleId = id

        _state.launchWithState {
            val detail = detailRepository.load(id)
            // 进详情即记一次浏览历史；记历史失败不该让正文也显示不出来
            runCatching { historyRepository.record(detail.article) }
            detail
        }

        viewModelScope.launch {
            favoriteRepository.observeIsFavorite(id).collect { _isFavorite.value = it }
        }
    }

    /** 切换收藏。按钮状态由 Room Flow 回流，这里只管提示 */
    fun toggleFavorite() {
        val detail = (_state.value as? UiState.Success)?.data ?: return
        launchCatching {
            val added = favoriteRepository.toggle(detail.article)
            sendEffect(
                UiEffect.ShowMessage(
                    UiMessage.Res(
                        if (added) {
                            R.string.article_detail_favorite_added
                        } else {
                            R.string.article_detail_favorite_removed
                        },
                    ),
                ),
            )
        }
    }
}
