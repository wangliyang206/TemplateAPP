package com.template.app.ui.login

import com.template.app.data.api.ApiService
import com.template.app.data.model.LoginParams
import com.template.app.data.model.User
import com.template.core.base.BaseRepository
import com.template.core.datastore.DataStoreManager
import com.template.core.db.UserDao
import com.template.core.db.UserEntity
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStoreManager,
    private val userDao: UserDao
) : BaseRepository() {

    suspend fun login(phone: String, password: String): User {
        val params = LoginParams(phone, password)
        val request = buildApiRequest(params)
        val response = apiCall { safeApi { apiService.login(request) } }
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
}
