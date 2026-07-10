package com.template.app.ui.mine

import androidx.lifecycle.viewModelScope
import com.template.core.base.BaseViewModel
import com.template.core.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : BaseViewModel() {

    /**
     * 用户信息状态流，UI 直接观察此状态
     */
    val userState: StateFlow<UserUiState> = dataStore.userFlow.map { userPref ->
        if (userPref.token.isNotEmpty()) {
            UserUiState.LoggedIn(
                userName = userPref.userName,
                phone = userPref.phone,
                avatar = userPref.avatar
            )
        } else {
            UserUiState.NotLoggedIn
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserUiState.NotLoggedIn
    )

    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            dataStore.clearUser()
        }
    }

    sealed class UserUiState {
        object NotLoggedIn : UserUiState()
        data class LoggedIn(
            val userName: String,
            val phone: String,
            val avatar: String
        ) : UserUiState()
    }
}