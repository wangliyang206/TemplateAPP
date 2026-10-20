package com.template.app.ui.category

import com.template.app.data.model.Category
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 分类页 ViewModel —— 「列表页」的标准写法：
 * 继承 BaseViewModel，只暴露一个 State，加载失败由基类统一转成 UiState.Error。
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<Category>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Category>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.launchWithState { repository.load() }
    }
}
