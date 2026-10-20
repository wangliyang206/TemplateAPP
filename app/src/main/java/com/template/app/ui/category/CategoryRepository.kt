package com.template.app.ui.category

import com.template.app.data.api.ApiService
import com.template.app.data.model.Category
import com.template.app.data.model.CategoryListParams
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory
) : BaseRepository() {

    suspend fun load(): List<Category> = apiCall {
        safeApi { apiService.getCategoryList(requestFactory.build(CategoryListParams(type = 0))) }
    }
}
