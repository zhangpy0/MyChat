package top.zhangpy.mychat.ui.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class MessageListItem {

    public int id;

    public boolean isMe;

    public String messageType; // text, image, file

    public String filePath;

    public String content;

    public Timestamp sendTime;

    public String fileName;

    public Long fileSize;

}
