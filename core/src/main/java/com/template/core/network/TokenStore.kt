package com.template.core.network

import com.template.core.datastore.DataStoreManager
import javax.inject.Inject

/**
 * Token 读写的抽象。
 *
 * 抽出接口的目的：TokenAuthenticator 的单测不需要 Android Context / DataStore。
 */
interface TokenStore {

    suspend fun currentToken(): String

    suspend fun updateToken(token: String)

    suspend fun clearToken()
}

/**
 * 基于 DataStore 的实现（模板默认）。
 * 若后续换成 MMKV / 加密存储，只需替换这个类。
 */
class DataStoreTokenStore @Inject constructor(private val dataStore: DataStoreManager) : TokenStore {

    override suspend fun currentToken(): String = dataStore.getUserSync().token

    override suspend fun updateToken(token: String) {
        val user = dataStore.getUserSync()
        dataStore.saveUser(
            userId = user.userId,
            token = token,
            userName = user.userName,
            phone = user.phone,
            avatar = user.avatar,
        )
    }

    override suspend fun clearToken() {
        dataStore.clearUser()
    }
}
