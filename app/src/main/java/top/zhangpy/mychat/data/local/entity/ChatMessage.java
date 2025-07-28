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
    public Integer messageId;

    @ColumnInfo(name = "sender_id")
    public Integer senderId;

    @ColumnInfo(name = "group_id")
    public Integer groupId; // 0 private message , > 1000000 group message

    @ColumnInfo(name = "receiver_id")
    public Integer receiverId;

    @ColumnInfo(name = "receiver_type")
    public String receiverType; // user, group

    @ColumnInfo(name = "send_time")
    public Timestamp sendTime;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "message_type")
    public String messageType; // text, image, file

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "file_id")
    public Integer fileId;

    @ColumnInfo(name = "is_read")
    public Boolean isRead;

    @ColumnInfo(name = "is_download")
    public Boolean isDownload;

    @Override
    public int hashCode() {
        return messageId;
    }
}
