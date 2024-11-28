package top.zhangpy.mychat.data.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.ServerMessageModel;


/*
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

```
1.chatMessage.setContent(userId + ":friend request get"); userId 对你发起了好友请求
2.chatMessage.setContent(userId + ":" + groupId + ":group request get"); userId 对你的groupId发起了入群请求
3.chatMessage.setContent(userId + ":friend request has been processed"); userId 已经处理了你的好友请求
4.chatMessage.setContent(groupId + ":group request has been processed"); groupId 已经处理了你的入群请求
5.chatMessage.setContent(groupId + ":remove you from group"); groupId 已经将你移出了群聊
```

1: update contactApply
2: update contactApply and contact(Friend)
3: update contactApply and contact(Group)
4: update contact(Group)


Received message: ServerMessage(senderId=11111, receiverId=11113, groupId=0, content=hahaha3, time=1732371614, messageType=text)
Received message: ServerMessage(senderId=0, receiverId=11113, groupId=0, content=11112:friend request get, time=1732371775, messageType=text)
 */
public class ServerMessageMapper {

    public static ServerMessageModel mapToServerMessageModel(String receivedMessage) {
        String regex = "ServerMessage\\(senderId=(\\d+), receiverId=(\\d+), groupId=(\\d+), content=(.*?), time=(\\d+), messageType=(\\w+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(receivedMessage);

        if (matcher.find()) {
            // 提取字段值
            Integer senderId = Integer.valueOf(matcher.group(1));
            Integer receiverId = Integer.valueOf(matcher.group(2));
            Integer groupId = Integer.valueOf(matcher.group(3));
            String content = matcher.group(4);
            Long time = Long.valueOf(matcher.group(5));
            String messageType = matcher.group(6);

            // 构造 ServerMessageModel
            ServerMessageModel messageModel = new ServerMessageModel(senderId, receiverId, groupId, content, messageType);
            messageModel.setTime(time); // 手动设置时间
            return messageModel;
        } else {
            throw new IllegalArgumentException("Invalid message format: " + receivedMessage);
        }
    }

    public static boolean isServerMessage(ServerMessageModel serverMessageModel) {
        Integer senderId = serverMessageModel.getSenderId();
        Integer groupId = serverMessageModel.getGroupId();
        return senderId.equals(0) && groupId.equals(0);
    }

    public static ChatMessageModel mapToChatMessageModel(ServerMessageModel serverMessageModel) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.setSenderId(serverMessageModel.getSenderId());
        chatMessageModel.setGroupId(serverMessageModel.getGroupId());
        chatMessageModel.setReceiverId(serverMessageModel.getReceiverId());
        if (serverMessageModel.getGroupId() == 0) {
            chatMessageModel.setReceiverType("user");
        } else {
            chatMessageModel.setReceiverType("group");
        }
        chatMessageModel.setSendTime(serverMessageModel.getTime());
        chatMessageModel.setMessageType(serverMessageModel.getMessageType());
        chatMessageModel.setContent(serverMessageModel.getContent());
        return chatMessageModel;
    }

    public static int getServerMessageType(ServerMessageModel serverMessageModel) {
        String content = serverMessageModel.getContent();
        String[] split = content.split(":");
        String serverText = split[split.length - 1];
        return switch (serverText) {
            case "friend request get", "group request get" -> 1;
            case "friend request has been processed" -> 2;
            case "group request has been processed" -> 3;
            case "remove you from group" -> 4;
            default -> 0;
        };
    }
}
