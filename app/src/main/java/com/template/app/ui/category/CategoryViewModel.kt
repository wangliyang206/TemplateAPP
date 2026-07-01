package com.template.app.ui.category

import com.template.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : BaseViewModel() {

    fun loadData() {
        launchWithLoading {
            repository.loadData()
        }
    }
}
