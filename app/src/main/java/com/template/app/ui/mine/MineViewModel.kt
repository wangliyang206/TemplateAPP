package com.template.app.ui.mine

import androidx.lifecycle.viewModelScope
import com.template.core.base.BaseViewModel
import com.template.core.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : BaseViewModel() {

    fun logout() {
        viewModelScope.launch {
            dataStore.clearUser()
        }
    }
}
