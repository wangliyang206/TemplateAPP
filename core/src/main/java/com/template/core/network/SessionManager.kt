package com.template.core.network

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局登录态管理。
 *
 * 401 且刷新失败时由 [TokenAuthenticator] 调用 [expire]，
 * Application 层订阅 [expired] 统一跳转登录页——业务页面不需要写任何登出逻辑。
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStore: TokenStore
) {

    private val _expired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    suspend fun expire() {
        tokenStore.clearToken()
        _expired.tryEmit(Unit)
    }
}
