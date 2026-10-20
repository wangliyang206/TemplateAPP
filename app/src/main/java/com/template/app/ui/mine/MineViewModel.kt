package com.template.app.ui.mine

import androidx.lifecycle.viewModelScope
import com.template.app.data.session.SessionRepository
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 「我的」页面 ViewModel。
 *
 * 登录态由 [SessionRepository] 的 Flow 驱动（单一真相），
 * 登录/登出后页面自动刷新，不需要手动通知。
 */
@HiltViewModel
class MineViewModel @Inject constructor(private val sessionRepository: SessionRepository) : BaseViewModel() {

    val userState: StateFlow<UserUiState> = sessionRepository.session
        .map { session ->
            if (session.isLoggedIn) {
                UserUiState.LoggedIn(
                    userName = session.userName,
                    phone = session.phone,
                    avatar = session.avatar,
                )
            } else {
                UserUiState.NotLoggedIn
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserUiState.NotLoggedIn,
        )

    fun logout() = launchCatching {
        sessionRepository.clear()
    }

    sealed interface UserUiState {
        data object NotLoggedIn : UserUiState

        data class LoggedIn(val userName: String, val phone: String, val avatar: String) : UserUiState
    }
}
