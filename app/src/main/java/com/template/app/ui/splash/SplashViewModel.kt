package com.template.app.ui.splash

import androidx.lifecycle.viewModelScope
import com.template.core.base.BaseViewModel
import com.template.core.config.CoreConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel() {

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain.asStateFlow()

    init {
        viewModelScope.launch {
            delay(CoreConfig.SPLASH_DELAY_MS)
            _navigateToMain.value = true
        }
    }
}
