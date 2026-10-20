package com.template.app.ui.register

import com.template.app.R
import com.template.app.data.model.User
import com.template.core.base.BaseViewModel
import com.template.core.ext.isPhone
import com.template.core.state.UiState
import com.template.core.state.of
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: RegisterRepository) : BaseViewModel() {

    // Idle：页面刚打开，还没提交过。用 Loading 会导致按钮一进来就被禁用。
    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    fun register(phone: String, password: String, confirmPassword: String) {
        validate(phone, password, confirmPassword)?.let {
            _registerState.value = it
            return
        }
        _registerState.launchWithState {
            repository.register(phone, password)
        }
    }

    /**
     * 入参校验。
     *
     * 两条模板约定：
     * 1. 错误文案用 [R.string] 资源，不在 ViewModel 里拼中文 —— 否则无法国际化；
     * 2. 手机号规则与登录页共用 `isPhone()`，避免两处正则各自演化。
     *
     * @return null 表示校验通过
     */
    private fun validate(phone: String, password: String, confirmPassword: String): UiState<User>? = when {
        !phone.isPhone() -> UiState.Error(of(R.string.register_phone_invalid))
        password.length < MIN_PASSWORD_LENGTH ->
            UiState.Error(of(R.string.register_password_too_short, MIN_PASSWORD_LENGTH))
        password != confirmPassword -> UiState.Error(of(R.string.register_password_mismatch))
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
