package com.template.core.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.template.core.base.BaseViewModel
import com.template.core.state.UiEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 在 STARTED 状态收集一次性副作用，页面不可见时不消费。
 */
fun LifecycleOwner.collectEffects(viewModel: BaseViewModel, onEffect: (UiEffect) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect(onEffect)
        }
    }
}

/**
 * 在 STARTED 状态收集 StateFlow / Flow，避免后台无谓刷新。
 */
fun <T> LifecycleOwner.collectFlow(flow: Flow<T>, onValue: (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { onValue(it) }
        }
    }
}
