package top.zhangpy.mychat.data.remote.model;


import lombok.Data;

/*
List<ChatMessage> chatMessages = chatMessageService.getChatMessagesAfterTime(Integer.valueOf(userId), timestamp);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.nullToEmpty();
            chatMessage.setFilePath("");
        }

    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    @TableField("sender_id")
    private Integer senderId;

    @TableField("receiver_id")
    private Integer receiverId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("receiver_type")
    private String receiverType;

    @TableField("send_time")
    private Timestamp sendTime;

    @TableField("content")
    private String content;

    @TableField("message_type")
    private String messageType;

    @TableField("file_path")
    private String filePath;


 */
@Data
public class ChatMessageModel {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private Integer groupId;
    private String receiverType;
    private Long sendTime;
    private String content;
    private String messageType;
    private String filePath;
}
