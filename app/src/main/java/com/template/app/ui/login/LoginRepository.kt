package com.template.app.ui.login

import com.template.app.data.api.ApiService
import com.template.app.data.model.LoginParams
import com.template.app.data.model.User
import com.template.app.data.session.SessionRepository
import com.template.core.base.BaseRepository
import com.template.core.network.ApiRequestFactory
import javax.inject.Inject

/**
 * 登录仓储：只做「认证 + 落会话」两件事。
 *
 * 本地持久化交给 [SessionRepository]，网络请求交给 [ApiService]，
 * 这个类只负责编排，不再三合一。
 */
class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
    private val sessionRepository: SessionRepository
) : BaseRepository() {

    suspend fun login(phone: String, password: String): User {
        // 登录接口匿名访问，不带 token
        val request = requestFactory.build(LoginParams(phone, password), authenticated = false)
        val user = apiCall { safeApi { apiService.login(request) } }
        sessionRepository.save(user)
        return user
    }
}
