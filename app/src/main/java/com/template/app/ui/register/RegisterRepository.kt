package com.template.app.ui.register

import com.template.app.data.api.ApiService
import com.template.app.data.model.RegisterParams
import com.template.app.data.model.User
import com.template.app.data.session.SessionRepository
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

class RegisterRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
    private val sessionRepository: SessionRepository,
) : BaseRepository() {

    suspend fun register(phone: String, password: String): User {
        val request = requestFactory.build(RegisterParams(phone, password), authenticated = false)
        val user = apiCall { safeApi { apiService.register(request) } }
        sessionRepository.save(user)
        return user
    }
}
