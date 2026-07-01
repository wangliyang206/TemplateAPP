package com.template.app.update

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.template.app.data.api.ApiService
import com.template.app.data.model.UpdateInfo
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val apiService: ApiService
) : BaseViewModel() {

    private val _updateState = MutableStateFlow<UiState<UpdateInfo>>(UiState.Loading)
    val updateState: StateFlow<UiState<UpdateInfo>> = _updateState.asStateFlow()

    private val downloadManager = AppDownloadManager()
    val downloadState = downloadManager.state

    fun checkUpdate(versionCode: Int) {
        _updateState.launchWithState {
            val resp = apiService.checkUpdate(versionCode)
            if (resp.code == 200 && resp.data != null) {
                resp.data!!
            } else {
                throw Exception(resp.message)
            }
        }
    }

    fun downloadApk(context: Context, updateInfo: UpdateInfo) {
        viewModelScope.launch {
            downloadManager.download(context, updateInfo.downloadUrl, "template_app_${updateInfo.versionCode}.apk")
        }
    }

    fun installApk(context: Context, file: java.io.File) {
        UpdateChecker.installApk(context, file)
    }
}
