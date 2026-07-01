package com.template.app.ui.home

import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : BaseViewModel() {

    fun loadData() {
        launchWithLoading {
            repository.loadData()
        }
    }
}
