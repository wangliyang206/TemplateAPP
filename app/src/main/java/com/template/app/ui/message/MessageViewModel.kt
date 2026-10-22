package com.template.app.ui.message

import com.template.app.data.message.MessageCenter
import com.template.app.data.model.Message
import com.template.core.base.BaseViewModel
import com.template.core.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 消息页 ViewModel。
 *
 * 数据真相源在 [MessageCenter]（单例），这里只把它映射成页面状态，
 * 并负责"点击即已读"的转发。
 */
@HiltViewModel
class MessageViewModel @Inject constructor(private val messageCenter: MessageCenter) : BaseViewModel() {

    private val _state = MutableStateFlow<UiState<List<Message>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Message>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.launchWithState {
            messageCenter.refresh()
            messageCenter.messages.value
        }
    }

    fun markRead(message: Message) {
        if (message.isRead) return
        messageCenter.markRead(message.id)
        _state.value = UiState.Success(messageCenter.messages.value)
    }

    fun markAllRead() {
        if (messageCenter.unreadCount.value == 0) return
        messageCenter.markAllRead()
        _state.value = UiState.Success(messageCenter.messages.value)
    }
}
