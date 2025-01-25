package top.zhangpy.mychat.ui.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class MessageListItem {

    private int id;

    private boolean isMe;

    private String messageType; // text, image, file

    private String filePath;

    private String content;

    private Timestamp sendTime;

    private String fileName;

    private Long fileSize;

}
