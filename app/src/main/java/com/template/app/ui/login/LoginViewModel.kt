package com.template.app.ui.login

import com.template.app.data.model.User
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: LoginRepository) : BaseViewModel() {

    // 必须是 Idle 而不是 Loading：Loading 会让登录页一进来就把按钮禁用，
    // 用户看到的是「按钮点不动」，且没有任何请求在进行。
    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    fun login(phone: String, password: String) {
        _loginState.launchWithState {
            repository.login(phone, password)
        }
    }
}
