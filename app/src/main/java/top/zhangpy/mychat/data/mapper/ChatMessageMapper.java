package top.zhangpy.mychat.data.mapper;

import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.util.Converter;

public class ChatMessageMapper {

    public static ChatMessage mapToChatMessage(ChatMessageModel chatMessageModel) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(Integer.parseInt(String.valueOf(chatMessageModel.getSenderId())));
        chatMessage.setGroupId(Integer.parseInt(String.valueOf(chatMessageModel.getGroupId())));
        chatMessage.setReceiverType(chatMessageModel.getReceiverType());
        chatMessage.setReceiverId(Integer.parseInt(String.valueOf(chatMessageModel.getReceiverId())));
        chatMessage.setSendTime(Converter.fromUnixTimestamp(chatMessageModel.getSendTime()));
        chatMessage.setMessageType(chatMessageModel.getMessageType());
        chatMessage.setContent(chatMessageModel.getContent());
        chatMessage.setFilePath("");

        chatMessage.setFileId(0);
        chatMessage.setFileName("");

        if (chatMessageModel.getMessageType().equals("text")) {
            chatMessage.setFileName("");
        } else {
            String content = chatMessageModel.getContent();
            String[] split = content.split(":");
            chatMessage.setFileId(Integer.parseInt(split[0]));
            Long fileSize = Long.parseLong(split[split.length - 1]);
            StringBuilder fileName = new StringBuilder();
            for (int i = 1; i < split.length - 1; i++) {
                fileName.append(split[i]);
            }
            chatMessage.setFileName(fileName.toString());
        }

        chatMessage.setIsRead(false);
        chatMessage.setIsDownload(false);
        return chatMessage;
    }
}
