package com.template.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * 会话持久化抽象：登录态（用户信息 + token）的读写。
 *
 * 与 [com.template.core.network.TokenStore] 同一套路——面向接口而不是面向
 * [DataStoreManager] 具体类，让依赖它的 Repository 能在纯 JVM 单测里
 * 用内存假实现替换，不必拉起 Robolectric + 真实 DataStore。
 *
 * 生产实现是 [DataStoreManager]（Hilt 在 DataStoreModule 绑定）。
 */
interface SessionStore {

    /** 用户信息的响应式读取流 */
    val userFlow: Flow<DataStoreManager.UserPref>

    /**
     * 保存用户会话。[avatar] 传 null 表示清除已有头像（登录接口可能不带）。
     */
    suspend fun saveUser(userId: String, token: String, userName: String, phone: String, avatar: String?)

    /** 清空会话（登出 / token 失效） */
    suspend fun clearUser()
}
