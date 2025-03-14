package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

import lombok.Data;

// 每个聊天对象(好友或群组)使用单独的数据库表存储聊天记录，每个库表的表名为 chat_messages

@Data
@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    private Integer messageId;

    @ColumnInfo(name = "sender_id")
    private Integer senderId;

    @ColumnInfo(name = "group_id")
    private Integer groupId; // 0 private message , > 1000000 group message

    @ColumnInfo(name = "receiver_id")
    private Integer receiverId;

    @ColumnInfo(name = "receiver_type")
    private String receiverType; // user, group

    @ColumnInfo(name = "send_time")
    private Timestamp sendTime;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "message_type")
    private String messageType; // text, image, file

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "file_name")
    private String fileName;

    @ColumnInfo(name = "file_id")
    private Integer fileId;

    @ColumnInfo(name = "is_read")
    private Boolean isRead;

    @ColumnInfo(name = "is_download")
    private Boolean isDownload;

    @Override
    public int hashCode() {
        return messageId;
    }
}
