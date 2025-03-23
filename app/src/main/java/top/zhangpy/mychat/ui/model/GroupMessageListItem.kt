package top.zhangpy.mychat.ui.model

import java.sql.Timestamp

data class GroupMessageListItem(
    val id: Int,
    val senderId: Int,         // 发送者ID（替换isMe）
    val senderName: String,    // 发送者昵称
    val messageType: String,   // text/image/file
    val filePath: String?,
    val content: String?,
    val sendTime: Timestamp,
    val fileName: String?,
    val fileSize: Long?
) {
    // 扩展属性：判断是否是自己发送的消息
    val isMe: Boolean
        get() = senderId == CURRENT_USER_ID // 需要替换实际的当前用户ID常量

    companion object {
        // 这里需要在实际项目中注入或设置当前用户ID
        const val CURRENT_USER_ID = 0
    }
}