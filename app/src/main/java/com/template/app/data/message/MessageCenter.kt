package com.template.app.data.message

import com.template.app.data.api.ApiService
import com.template.app.data.model.Message
import com.template.app.data.model.MessageListParams
import com.template.core.network.ApiRequestFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 站内消息中心 —— **跨页面共享状态**的参考实现。
 *
 * 为什么不是普通 Repository：消息的"未读数"同时被两处消费——
 * 消息页的列表、底部导航的角标（在 MainActivity 里）。
 * 用 `@Singleton` + StateFlow 做单一真相源，避免两边各拉一次接口、各自算未读。
 *
 * 已读状态目前只存在内存（Mock 场景够用）；接真实后端时把 [markRead] 改成"先调接口再更新本地"。
 */
@Singleton
class MessageCenter @Inject constructor(
    private val apiService: ApiService,
    private val requestFactory: ApiRequestFactory,
) {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)

    /** 未读数，供底部导航角标使用 */
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    suspend fun refresh() {
        val response = apiService.getMessageList(requestFactory.build(MessageListParams()))
        val data = response.data ?: return
        // 已经标记过已读的保持已读，避免刷新把本地状态冲掉
        val readIds = _messages.value.filter { it.isRead }.map { it.id }.toSet()
        _messages.value = data.map { if (it.id in readIds) it.copy(isRead = true) else it }
        syncUnreadCount()
    }

    fun markRead(id: Long) {
        _messages.value = _messages.value.map { if (it.id == id) it.copy(isRead = true) else it }
        syncUnreadCount()
    }

    fun markAllRead() {
        _messages.value = _messages.value.map { it.copy(isRead = true) }
        syncUnreadCount()
    }

    private fun syncUnreadCount() {
        _unreadCount.value = _messages.value.count { !it.isRead }
    }
}
