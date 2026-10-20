package com.template.app.data.session

import com.template.app.data.model.User
import com.template.core.datastore.SessionStore
import com.template.core.db.UserDao
import com.template.core.db.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话数据源：唯一负责「本地登录态」的读写。
 *
 * 为什么单独抽出来：
 * - 上一版 LoginRepository 同时打接口、写 DataStore、写 Room，职责过载；
 * - 现在网络归 AuthRepository，本地归 SessionRepository，边界清晰；
 * - MineViewModel 等页面只依赖本类，不直接碰 DataStore。
 *
 * 持久化走 [SessionStore] 接口（生产实现 DataStoreManager），
 * 单测里换成内存假实现即可，不必拉起 Robolectric。
 */
@Singleton
class SessionRepository @Inject constructor(private val dataStore: SessionStore, private val userDao: UserDao) {

    val session: Flow<Session> = dataStore.userFlow.map { pref ->
        Session(
            userId = pref.userId,
            token = pref.token,
            userName = pref.userName,
            phone = pref.phone,
            avatar = pref.avatar,
        )
    }

    suspend fun save(user: User) {
        dataStore.saveUser(
            userId = user.userId,
            token = user.token,
            userName = user.userName,
            phone = user.userPhone,
            avatar = user.avatar,
        )
        userDao.insert(
            UserEntity(
                userId = user.userId,
                userName = user.userName,
                phone = user.userPhone,
                avatar = user.avatar,
            ),
        )
    }

    suspend fun clear() {
        dataStore.clearUser()
        userDao.clear()
    }
}

data class Session(val userId: String, val token: String, val userName: String, val phone: String, val avatar: String) {
    val isLoggedIn: Boolean get() = token.isNotEmpty()
}
