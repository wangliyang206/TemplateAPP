package com.template.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.core.state.UiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    protected fun <T> MutableStateFlow<UiState<T>>.launchWithState(
        block: suspend () -> T
    ) {
        val flow = this
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            val msg = throwable.message ?: "操作失败"
            flow.value = UiState.Error(msg)
            _loadingState.value = false
        }) {
            _loadingState.value = true
            flow.value = UiState.Loading
            val result = block()
            flow.value = UiState.Success(result)
            _loadingState.value = false
        }
    }

    protected fun launchWithLoading(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            _loadingState.value = false
        }) {
            _loadingState.value = true
            block()
            _loadingState.value = false
        }
    }
}
