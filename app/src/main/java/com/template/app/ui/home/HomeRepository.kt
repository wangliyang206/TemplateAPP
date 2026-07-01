package com.template.app.ui.home

import com.template.core.base.BaseRepository
import javax.inject.Inject

class HomeRepository @Inject constructor() : BaseRepository() {

    suspend fun loadData(): String {
        return io { "首页数据" }
    }
}
