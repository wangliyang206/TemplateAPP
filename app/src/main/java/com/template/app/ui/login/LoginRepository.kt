package com.template.app.ui.login

import com.template.app.data.api.ApiService
import com.template.app.data.model.User
import com.template.core.base.BaseRepository
import com.template.core.datastore.DataStoreManager
import com.template.core.db.UserDao
import com.template.core.db.UserEntity
import com.template.core.network.ApiResult
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStoreManager,
    private val userDao: UserDao
) : BaseRepository() {

    suspend fun login(phone: String, password: String): User {
        val params = mapOf("phone" to phone, "password" to password)
        val response = apiCall { safeApi { apiService.login(params) } }
        dataStore.saveUser(response.userId, response.token, response.userName)
        userDao.insert(
            UserEntity(
                userId = response.userId,
                userName = response.userName,
                phone = response.phone,
                avatar = response.avatar
            )
        )
        return response
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
