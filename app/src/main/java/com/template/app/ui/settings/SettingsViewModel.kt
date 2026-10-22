package com.template.app.ui.settings

import androidx.lifecycle.viewModelScope
import com.template.app.data.settings.AppSettings
import com.template.app.data.settings.SettingsStore
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 设置页 ViewModel —— 「只读配置 + 写配置」的最小范式。
 *
 * 注意这里**没有** UiState：设置项是本地存储、必然成功、无分页无网络，
 * 硬套 Idle/Loading/Success 只会让页面闪一下 loading。
 * 状态形状跟着数据源走，不要为了统一而统一。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val store: SettingsStore) : BaseViewModel() {

    val settings: StateFlow<AppSettings> = store.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = AppSettings(),
    )

    fun setWifiOnlyDownload(enabled: Boolean) = launchCatching { store.setWifiOnlyDownload(enabled) }

    fun setPushEnabled(enabled: Boolean) = launchCatching { store.setPushEnabled(enabled) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
