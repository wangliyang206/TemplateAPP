package com.template.app.ui.mine

import androidx.lifecycle.viewModelScope
import com.template.app.data.repository.FavoriteRepository
import com.template.app.data.repository.HistoryRepository
import com.template.app.data.repository.UploadRepository
import com.template.app.data.session.SessionRepository
import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 「我的」页面 ViewModel。
 *
 * 登录态由 [SessionRepository] 的 Flow 驱动（单一真相），
 * 登录/登出后页面自动刷新，不需要手动通知。
 *
 * 上传部分是 [com.template.core.network.FileUploadManager] 的完整调用示例：
 * 进度走独立 StateFlow（高频更新不污染 userState），结果用上传自己的状态表达。
 */
@HiltViewModel
class MineViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val uploadRepository: UploadRepository,
    favoriteRepository: FavoriteRepository,
    historyRepository: HistoryRepository,
) : BaseViewModel() {

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

    /**
     * 「我的收藏 / 浏览历史」右侧的条数。
     *
     * 直接观察 Room 的 `COUNT(*)`，所以在详情页收藏或看完文章返回时数字就已经是最新的，
     * 不需要在 `onResume` 里重新查一遍。
     */
    val favoriteCount: StateFlow<Int> = favoriteRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val historyCount: StateFlow<Int> = historyRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    fun logout() = launchCatching {
        sessionRepository.clear()
    }

    /** 触发一次演示上传；进行中重复点击直接忽略 */
    fun uploadDemo() {
        if (_uploadState.value is UploadUiState.Uploading) return
        viewModelScope.launch {
            _uploadState.value = UploadUiState.Uploading(progress = 0)
            val result = uploadRepository.uploadDemoFile { progress ->
                _uploadState.value = UploadUiState.Uploading(progress)
            }
            _uploadState.value = result.fold(
                onSuccess = { body -> UploadUiState.Done(body.take(SERVER_BODY_PREVIEW_LENGTH)) },
                onFailure = { e -> UploadUiState.Failed(e.message ?: e.javaClass.simpleName) },
            )
        }
    }

    sealed interface UserUiState {
        data object NotLoggedIn : UserUiState

        data class LoggedIn(val userName: String, val phone: String, val avatar: String) : UserUiState
    }

    /** 上传演示的状态；真实业务里通常只保留 Uploading(progress) + 结果 toast */
    sealed interface UploadUiState {
        data object Idle : UploadUiState

        data class Uploading(val progress: Int) : UploadUiState

        data class Done(val serverBody: String) : UploadUiState

        data class Failed(val message: String) : UploadUiState
    }

    private companion object {
        const val SERVER_BODY_PREVIEW_LENGTH = 120
    }
}
