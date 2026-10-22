package com.template.app.data.model

/**
 * 消息类型。
 *
 * 用枚举而不是 Int 常量：后续加类型时编译期就能发现 when 分支漏处理
 * （Kotlin 的 sealed/枚举穷尽检查）。
 */
enum class MessageType {
    /** 系统通知：版本更新、运营公告 */
    SYSTEM,

    /** 互动消息：评论、点赞、关注 */
    INTERACT,
}

/**
 * 站内消息。
 *
 * 模板里由 Mock 提供，且已读状态只存在内存中（重启复位）。
 * 接真实后端时把 isRead 换成服务端字段 + 本地 Room 缓存即可。
 */
data class Message(
    val id: Long,
    val title: String,
    val content: String,
    val time: String,
    val type: MessageType,
    val isRead: Boolean = false,
)
