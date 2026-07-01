package com.template.app.ui.register

import com.template.app.data.model.User
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: RegisterRepository
) : BaseViewModel() {

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    fun register(phone: String, password: String, confirmPassword: String) {
        if (phone.length != 11) {
            _registerState.value = UiState.Error("请输入正确的手机号")
            return
        }
        if (password.length < 6) {
            _registerState.value = UiState.Error("密码至少6位")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = UiState.Error("两次密码不一致")
            return
        }
        _registerState.launchWithState {
            repository.register(phone, password)
        }
    }
}
