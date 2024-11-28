package top.zhangpy.mychat.data.remote.model;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class ServerMessageModel implements Serializable {
    private Integer senderId;
    private Integer receiverId;
    private Integer groupId; // 0: user, other: group
    private String content;
    private Long time;
    private String messageType; // 0: text, 1: image, 2: file

    public ServerMessageModel(Integer senderId, Integer receiverId, Integer groupId, String content, String messageType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.content = content;
        this.time = Instant.now().getEpochSecond();
        this.messageType = messageType;
    }
}
