package com.template.app.ui.discover

import com.template.app.data.model.Category
import com.template.app.data.model.RankArticle
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 发现页 ViewModel —— **多数据源并行加载**的参考实现。
 *
 * 分类和排行是两个独立接口，串行发起会让首屏等待时间翻倍；
 * 这里用 `async` 并行，任一失败都会整体走 `UiState.Error`（`await` 会把异常抛出来）。
 *
 * 板块切块（每行 2 个分类）也放在 ViewModel：UI 只负责画，不做数据编排。
 */
@HiltViewModel
class DiscoverViewModel @Inject constructor(private val repository: DiscoverRepository) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<DiscoverItem>>>(UiState.Idle)
    val state: StateFlow<UiState<List<DiscoverItem>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.launchWithState {
            val (categories, ranks) = coroutineScope {
                val categoriesTask = async { repository.loadCategories() }
                val ranksTask = async { repository.loadRanks() }
                categoriesTask.await() to ranksTask.await()
            }
            buildItems(categories, ranks)
        }
    }

    /**
     * 兜底：真遇到空数据也要给页面一个"有内容结构"的列表，
     * 否则四态会因为 `state.data.isEmpty()` 直接判空，用户看不到板块标题。
     */
    private fun buildItems(categories: List<Category>, ranks: List<RankArticle>): List<DiscoverItem> = buildList {
        add(DiscoverItem.Section(SectionType.CATEGORY))
        categories.chunked(COLUMNS).forEach { add(DiscoverItem.CategoryRow(it)) }

        add(DiscoverItem.Section(SectionType.RANK))
        ranks.forEachIndexed { index, rank -> add(DiscoverItem.Rank(index + 1, rank)) }
    }

    private companion object {
        /** 分类卡片每行个数 */
        const val COLUMNS = 2
    }
}
