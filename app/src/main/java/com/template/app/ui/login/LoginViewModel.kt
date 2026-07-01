package com.template.app.ui.login

import androidx.lifecycle.viewModelScope
import com.template.app.data.model.User
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    fun login(phone: String, password: String) {
        _loginState.launchWithState {
            repository.login(phone, password)
        }
    }
}
