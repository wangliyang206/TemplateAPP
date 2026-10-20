package com.template.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.template.core.error.AppError
import com.template.core.error.AppException
import com.template.core.error.asUiMessage
import com.template.core.error.toAppError
import com.template.core.state.UiEffect
import com.template.core.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 所有 ViewModel 的统一基类。
 *
 * 设计取舍：**只提供能力，不规定状态形状**。
 * 上一版把 `UiState<T>` 强制成基类字段，导致 Paging、多数据源页面只能绕开基类，
 * 最终一个项目里出现三种写法。这里改为「组合」：
 *
 * - [launchWithState]：把一次挂起调用映射成 UiState（Loading → Success / Error）
 * - [launchCatching]：只关心成功与否，失败统一走副作用
 * - [effects]：一次性事件通道（Toast / 导航 / 登出）
 */
abstract class BaseViewModel : ViewModel() {

    private val _loadingState = MutableStateFlow(false)

    /** 页面级 loading，仅在需要单独控制时使用 */
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 一次性副作用流，UI 在 STARTED 状态收集 */
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    protected fun sendEffect(effect: UiEffect) {
        _effects.tryEmit(effect)
    }

    /**
     * 执行一次请求，并把结果写入 [state]。
     *
     * 取消异常会原样抛出，避免页面销毁时被当成「加载失败」。
     */
    protected fun <T> MutableStateFlow<UiState<T>>.launchWithState(
        showLoading: Boolean = true,
        block: suspend () -> T
    ) {
        val state = this
        viewModelScope.launch {
            if (showLoading) {
                _loadingState.value = true
                state.value = UiState.Loading
            }
            try {
                state.value = UiState.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                state.value = UiState.Error(e.toAppError().asUiMessage())
            } finally {
                if (showLoading) _loadingState.value = false
            }
        }
    }

    /**
     * 不改变页面状态，只处理失败（默认弹一条错误提示）。
     */
    protected fun launchCatching(
        onError: (AppError) -> Unit = { sendEffect(UiEffect.ShowMessage(it.asUiMessage())) },
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: AppException) {
                onError(e.error)
            } catch (e: Throwable) {
                onError(e.toAppError())
            }
        }
    }

    /**
     * 只维护 loading 开关，不产生状态数据。
     */
    protected fun launchWithLoading(block: suspend () -> Unit) {
        launchCatching(block = block)
    }
}
