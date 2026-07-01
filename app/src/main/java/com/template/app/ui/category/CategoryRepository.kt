package com.template.app.ui.category

import com.template.core.base.BaseRepository
import javax.inject.Inject

class CategoryRepository @Inject constructor() : BaseRepository() {

    suspend fun loadData(): String {
        return io { "分类数据" }
    }
}
