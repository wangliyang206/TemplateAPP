package com.template.app.ui.register

import com.template.app.data.api.ApiService
import com.template.app.data.model.User
import com.template.core.base.BaseRepository
import javax.inject.Inject

class RegisterRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun register(phone: String, password: String): User {
        val params = mapOf("phone" to phone, "password" to password)
        return apiCall { safeApi { apiService.register(params) } }
    }
}
