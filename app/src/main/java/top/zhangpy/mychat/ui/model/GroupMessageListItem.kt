package top.zhangpy.mychat.ui.model

import java.sql.Timestamp

data class GroupMessageListItem(
    var id: Int,
    var senderId: Int,         // 发送者ID（替换isMe）
    var senderName: String,    // 发送者昵称
    var messageType: String,   // text/image/file
    var filePath: String?,
    var content: String?,
    var sendTime: Timestamp,
    var fileName: String?,
    var fileSize: Long?
) {
    constructor() : this(0, 0, "",
        "", "", "",
        Timestamp(System.currentTimeMillis()), "", 0){

    }

    // 扩展属性：判断是否是自己发送的消息
    val isMe: Boolean
        get() = senderId == CURRENT_USER_ID // 需要替换实际的当前用户ID常量

    companion object {
        const val CURRENT_USER_ID = 0
    }
}