package com.template.app.update

import com.template.app.data.api.ApiService
import com.template.app.data.model.CheckUpdateParams
import com.template.app.data.model.UpdateInfo
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory
) : BaseRepository() {

    suspend fun checkUpdate(versionCode: Int): UpdateInfo =
        apiCall {
            safeApi {
                apiService.checkUpdate(requestFactory.build(CheckUpdateParams(versionCode)))
            }
        }
}
