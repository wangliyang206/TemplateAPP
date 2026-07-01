package com.template.app.ui.register

import com.template.app.data.api.ApiService
import com.template.app.data.model.User
import com.template.core.base.BaseRepository
import com.template.core.network.ApiResult
import javax.inject.Inject

class RegisterRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun register(phone: String, password: String): User {
        val params = mapOf("phone" to phone, "password" to password)
        return apiCall { safeApi { apiService.register(params) } }
    }

    private suspend fun <T> safeApi(call: suspend () -> com.template.app.data.model.BaseResponse<T>): ApiResult<T> {
        val resp = call()
        return if (resp.code == 200 && resp.data != null) {
            ApiResult.Success(resp.data)
        } else {
            ApiResult.Error(com.template.core.network.ApiException(resp.code, resp.message))
        }
    }
}
