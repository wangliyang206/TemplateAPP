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
        // 本接口的请求体
        val params = LoginParams(phone, password)
        // 构建最终请求体
        val request = buildApiRequest(params)
        // 发起请求
        val response = apiCall { safeApi { apiService.login(request) } }
        // 保存用户信息
        dataStore.saveUser(
            userId = response.userId,
            token = response.token,
            userName = response.userName,
            phone = response.userPhone,
            avatar = response.avatar
        )
        // 保存用户信息到数据库
        userDao.insert(
            UserEntity(
                userId = response.userId,
                userName = response.userName,
                phone = response.userPhone,
                avatar = response.avatar
            )
        )
        return response
    }
}
