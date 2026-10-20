package com.template.app.ui.home

import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 首页 ViewModel：一个 State、一个入口、一种错误处理。
 *
 * 对照上一版：
 * - 不再直接注入 ApiService（走 HomeRepository）；
 * - 不再 `catch (_: Exception) {}` 静默吞错（错误进 UiState.Error → 页面显示重试）；
 * - 不再用 postValue 拼 LiveData（统一 StateFlow）。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
    val state: StateFlow<UiState<HomeData>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.launchWithState { repository.load() }
    }
}
