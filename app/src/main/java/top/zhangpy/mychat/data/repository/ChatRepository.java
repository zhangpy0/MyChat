package top.zhangpy.mychat.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import top.zhangpy.mychat.data.exception.NetException;
import top.zhangpy.mychat.data.local.dao.DynamicChatDao;
import top.zhangpy.mychat.data.local.database.ChatMessageDatabaseManager;
import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.local.entity.Friend;
import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.remote.RetrofitClient;
import top.zhangpy.mychat.data.remote.api.ChatService;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.DownloadModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.ui.model.ChatListItem;
import top.zhangpy.mychat.ui.model.GroupMessageListItem;
import top.zhangpy.mychat.ui.model.MessageListItem;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;
import top.zhangpy.mychat.util.download.ProgressInputStream;
import top.zhangpy.mychat.util.download.ProgressListener;
import top.zhangpy.mychat.util.lock.ConcurrentLock;

public class ChatRepository {
    private final DynamicChatDao chatDao;
    private final ChatMessageDatabaseManager manager;

    private final ChatService chatService = RetrofitClient.chatService;

    private final ContactRepository contactRepository;

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final Context context;

    public ChatRepository() {
        this.context = null;
        this.manager = null;
        this.chatDao = null;
        this.contactRepository = new ContactRepository();
        this.groupRepository = new GroupRepository();
        this.userRepository = new UserRepository();
        Logger.initialize(null);
        Logger.enableLogging(true);
    }

    public ChatRepository(Context context) {
        this.context = context;
        this.manager = ChatMessageDatabaseManager.getInstance(context);
        this.chatDao = new DynamicChatDao(manager.getDatabase());
        this.contactRepository = new ContactRepository(context);
        this.groupRepository = new GroupRepository(context);
        this.userRepository = new UserRepository(context);
        Logger.initialize(context);
        Logger.enableLogging(true);
    }

    public static String getTableName(ChatMessage message) {
        if (message.getReceiverType().equals("user")) {
            return "chat_" + message.getSenderId() + "_" + message.getReceiverId();
        } else {
            return "group_" + message.getGroupId();
        }
    }

    public static String getTableName(Integer groupId) {
        return "group_" + groupId;
    }

    // senderId = otherId
    public static String getTableName(int senderId, int receiverId) {
        return "chat_" + senderId + "_" + receiverId;
    }

    public boolean isTableExist(String tableName) {
        return manager.isTableExist(tableName);
    }

    // 动态创建聊天表
    public void createChatTable(String tableName) {
        manager.createChatTable(tableName);
    }

    // 插入聊天消息
    public void insertMessage(String tableName, ChatMessage message) {
        chatDao.insertMessage(tableName, message);
    }

    public ChatMessage getMessageById(String tableName , int messageId) {
        return chatDao.getMessageByID(tableName, messageId);
    }

    public void updateMessage(String tableName, ChatMessage message) {
        chatDao.updateMessage(tableName, message);
    }

    // 获取聊天消息
    public List<ChatMessage> getMessages(String tableName) {
        return chatDao.getMessages(tableName);
    }

    // 查询 以[message_id - 49, message_id] 之间的消息
    public List<ChatMessage> getMessages(String tableName, int messageId) {
        return chatDao.getMessages(tableName, messageId);
    }

    // 删除聊天表
    public void deleteChatTable(String tableName) {
        manager.deleteChatTable(tableName);
    }

    // 删除单条消息
    public void deleteMessage(String tableName, int messageId) {
        chatDao.deleteMessage(tableName, messageId);
    }

    public int getMaxMessageId(String tableName) {
        return chatDao.getMaxMessageId(tableName);
    }


    // no file: path set null
    @ConcurrentLock(params = {0, 1, 2, 3, 4, 5, 6})
    public boolean sendMessage(String userId, String receiverId, String groupId, String receiverType, String content, String messageType, String token, String path) throws IOException {
        File file = null;
        MultipartBody.Part filePart = null;
        if (path != null) {
            if (path.startsWith("content://")) {
                file = new File(StorageHelper.getRealPathFromURI(context, path));
            } else {
                file = new File(path);
            }
//            file = new File(path);
            RequestBody requestBody = RequestBody.create(file, okhttp3.MediaType.parse("multipart/form-data"));
            filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        }
        else {
            RequestBody requestBody = RequestBody.create("noFile", okhttp3.MediaType.parse("multipart/form-data"));
            filePart = MultipartBody.Part.createFormData("noFile", "noFile", requestBody);
        }
        if (receiverType.equals("user")) {
            groupId = "0";
        }
        ResultModel res =  chatService.sendMessage(userId, receiverId, groupId, receiverType, content, messageType, token, filePart).execute().body();
        return NetException.responseCheck(res, 0);
    }

    public List<ChatMessageModel> getMessages(String userId, long time, String token) throws IOException {
//        chatService.getMessages(userId, time, token).enqueue(new Callback<ResultModel<List<ChatMessageModel>>>() {
//            @Override
//            public void onResponse(Call<ResultModel<List<ChatMessageModel>>> call, Response<ResultModel<List<ChatMessageModel>>> response) {
//                ResultModel<List<ChatMessageModel>> res = response.body();
//
//            }
//
//            @Override
//            public void onFailure(Call<ResultModel<List<ChatMessageModel>>> call, Throwable throwable) {
//
//            }
//        });
        ResultModel<List<ChatMessageModel>> res = chatService.getMessages(userId, time, token).execute().body();
        return NetException.responseCheck(res);
    }

    public DownloadModel downloadFile(String userId, String messageId, String token) throws IOException {
        Response<ResponseBody> response = chatService.downloadFile(userId, messageId, token).execute(); // 此时仅完成了 HTTP 响应头（Header）的接收，响应体（Body）数据 尚未开始传输。
        if (response.isSuccessful() && response.body() != null) {
            String fileName = response.headers().get("Content-Disposition").split("filename=")[1];
            InputStream is = response.body().byteStream(); // 未缓冲的原始输入流
//            is.close();
            return new DownloadModel(fileName, is);
        } else {
            throw new RuntimeException("failed to download file");
        }
    }


    public DownloadModel downloadFileWithProgress(String userId, String messageId, String token, ProgressListener listener) throws IOException {
        try {
            Response<ResponseBody> response = chatService.downloadFile(userId, messageId, token).execute();
            if (response.isSuccessful() && response.body() != null) {
                String fileName = response.headers().get("Content-Disposition").split("filename=")[1];
                long totalBytes = response.body().contentLength();
                InputStream rawStream = response.body().byteStream();
                InputStream progressStream = new ProgressInputStream(rawStream, totalBytes, listener);
                return new DownloadModel(fileName, progressStream);
            } else {
                throw new RuntimeException("failed to download file");
            }
        } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onError(e);
                });
        }
        return null;
    }

    public String downloadFile(Context context, String userId, String token, ChatMessage message) {
        String messageId = String.valueOf(message.getFileId());
        final String[] path = {null};

            try {
                DownloadModel downloadModel = downloadFile(userId, messageId, token);
                if (downloadModel == null) {
                    return null;
                }
                if (message.getReceiverType().equals("user")) {
                    path[0] = StorageHelper.saveFile(context, "chat", String.valueOf(message.getReceiverId()), String.valueOf(message.getSenderId()), downloadModel.getFileName(), downloadModel.getInputStream());
                } else {
                    path[0] = StorageHelper.saveFile(context, "chat_group", String.valueOf(message.getReceiverId()), String.valueOf(message.getGroupId()), downloadModel.getFileName(), downloadModel.getInputStream());
                }
                message.setFilePath(path[0]);
                message.setIsRead(true);
                message.setIsDownload(true);
                updateMessage(getTableName(message), message);
            } catch (IOException e) {
                Logger.e("ChatRepository", "download file failed", e);
            }

        return path[0];
    }

    // TODO saveFile listener 回调
    @ConcurrentLock(params = {1, 3})
    public String downloadFileWithProgress(Context context,
                                           String userId,
                                           String token,
                                           ChatMessage message,
                                           ProgressListener listener) {
        String messageId = String.valueOf(message.getFileId());
        final String[] path = {null};
        try {
            DownloadModel downloadModel = downloadFileWithProgress(userId, messageId, token, listener);
            if (downloadModel == null) {
                return null;
            }
            if (message.getReceiverType().equals("user")) {
                path[0] = StorageHelper.saveFile(context, "chat", String.valueOf(message.getReceiverId()), String.valueOf(message.getSenderId()), downloadModel.getFileName(), downloadModel.getInputStream());
            } else {
                path[0] = StorageHelper.saveFile(context, "chat_group", String.valueOf(message.getReceiverId()), String.valueOf(message.getGroupId()), downloadModel.getFileName(), downloadModel.getInputStream());
            }
            message.setFilePath(path[0]);
            message.setIsRead(true);
            message.setIsDownload(true);
            updateMessage(getTableName(message), message);
        } catch (IOException e) {
            Logger.e("ChatRepository", "download file failed", e);
        }
        return path[0];
    }

    public String downloadImage(Context context, String userId, String token, ChatMessage message) throws IOException {
        String messageId = String.valueOf(message.getFileId());
        final String[] path = {null};

        Logger.d("ChatRepository", "download image message Id: " + messageId);

        try {
            DownloadModel downloadModel = downloadFile(userId, messageId, token);
            if (downloadModel == null) {
                return null;
            }
            if (message.getReceiverType().equals("user")) {
                path[0] = StorageHelper.saveFile(context, "chat", String.valueOf(message.getReceiverId()), String.valueOf(message.getSenderId()), downloadModel.getFileName(), downloadModel.getInputStream());
            } else {
                path[0] = StorageHelper.saveFile(context, "chat_group", String.valueOf(message.getReceiverId()), String.valueOf(message.getGroupId()), downloadModel.getFileName(), downloadModel.getInputStream());
            }
            Logger.d("ChatRepository", "download image path: " + path[0]);
            message.setFilePath(path[0]);
            message.setIsRead(false);
            message.setIsDownload(true);
            updateMessage(getTableName(message), message);
        } catch (IOException e) {
            Logger.e("ChatRepository", "download image failed", e);
        }
        return path[0];
    }

    public boolean updateChatFromServer(Context context, String userId, String token, ChatMessage chatMessage) throws IOException {
        String tableName = getTableName(chatMessage);
        Integer friendId = chatMessage.getSenderId();
        Integer groupId = chatMessage.getGroupId();
        if (chatMessage.getReceiverType().equals("user")) {
            Friend friend = contactRepository.getFriendByFriendId(friendId);
            if (friend == null) {
                Logger.e("ChatRepository", "Friend not found: " + friendId);
            } else {
                friend.setMessageTime(chatMessage.getSendTime());
                contactRepository.updateFriend(friend);
            }
        } else {
            Group group = groupRepository.getGroupById(groupId);
            if (group == null) {
                Logger.e("ChatRepository", "Group not found: " + groupId);
            } else {
                group.setMessageTime(chatMessage.getSendTime());
                groupRepository.updateGroup(group);
            }
        }
        if (chatMessage.getMessageType().equals("image")) {
            insertMessage(tableName, chatMessage);
            ChatMessage messageLocal = chatDao.getLastMessage(tableName);
            return downloadImage(context, userId, token, messageLocal) != null;
        } else {
            insertMessage(tableName, chatMessage);
            return true;
        }
    }

    public List<ChatListItem> updateChatListFromLocal(String token, Integer userId) throws IOException {
        List<Friend> friends = contactRepository.getAllFriendsSortedByMessageTime();
        List<Group> groups = groupRepository.getAllGroupsSortedByMessageTime();
        Set<ChatListItem> chatListItemSet = new HashSet<>();
        boolean isUpdate = false;
        for (Friend friend : friends) {
            createChatTable(getTableName(friend.getFriendId(), friend.getUserId()));
            ChatMessage message = chatDao.getLastMessage(getTableName(friend.getFriendId(), friend.getUserId()));
            UserProfile friendProfile = userRepository.getUserProfileById(friend.getFriendId());
            if (friendProfile == null && !isUpdate) {
                contactRepository.updateFriendAndGroupFromServer(token, userId);
                friendProfile = userRepository.getUserProfileById(friend.getFriendId());
                isUpdate = true;
            }
            if (message != null) {
                ChatListItem chatListItem = new ChatListItem();
                chatListItem.setId(friend.getFriendId());
                chatListItem.setContactName(friendProfile.getNickname());
                chatListItem.setSenderName(friendProfile.getNickname());
                chatListItem.setContent(message.getContent());
                chatListItem.setTime(friend.getMessageTime());
                chatListItem.setUnreadCount(chatDao.getUnreadCount(getTableName(friend.getFriendId(), friend.getUserId())));
                chatListItem.setAvatarPath(friendProfile.getAvatarPath());
                chatListItem.resetContent(message.getMessageType());
                chatListItem.setAbsTime(friend.getMessageTime().getTime());
                chatListItemSet.add(chatListItem);
            } else {
                ChatListItem chatListItem = new ChatListItem();
                chatListItem.setId(friend.getFriendId());
                chatListItem.setContactName(friendProfile.getNickname());
                chatListItem.setSenderName(friendProfile.getNickname());
                chatListItem.setContent("");
                chatListItem.setTime(friend.getCreatedAt());
                chatListItem.setUnreadCount(0);
                chatListItem.setAvatarPath(friendProfile.getAvatarPath());
                chatListItem.resetContent();
                chatListItem.setAbsTime(friend.getCreatedAt().getTime());
                chatListItemSet.add(chatListItem);
            }
        }
        for (Group group : groups) {
            createChatTable(getTableName(group.getGroupId()));
            ChatMessage message = chatDao.getLastMessage(getTableName(group.getGroupId()));
            GroupInfo groupInfo = groupRepository.getGroupInfoById(group.getGroupId());
            UserProfile senderProfile;
            if (message != null) {
                senderProfile = userRepository.getUserProfileById(message.getSenderId());
                ChatListItem chatListItem = new ChatListItem();
                chatListItem.setId(group.getGroupId());
                chatListItem.setContactName(groupInfo.getGroupName());
                if (senderProfile == null) {
                    chatListItem.setSenderName("");
                } else {
                    chatListItem.setSenderName(senderProfile.getNickname());
                }
                chatListItem.setContent(message.getContent());
                chatListItem.setTime(group.getMessageTime());
                chatListItem.setAbsTime(group.getMessageTime().getTime());
                chatListItem.setUnreadCount(chatDao.getUnreadCount(getTableName(group.getGroupId())));
                chatListItem.setAvatarPath(groupInfo.getAvatarPath());
                chatListItem.resetContent(message.getMessageType());
                chatListItemSet.add(chatListItem);
            } else {
                ChatListItem chatListItem = new ChatListItem();
                chatListItem.setId(group.getGroupId());
                chatListItem.setContactName(groupInfo.getGroupName());
                chatListItem.setSenderName("");
                chatListItem.setContent("");
                chatListItem.setTime(group.getMessageTime());
                chatListItem.setUnreadCount(0);
                chatListItem.setAvatarPath(groupInfo.getAvatarPath());
                chatListItem.resetContent();
                chatListItem.setAbsTime(group.getMessageTime().getTime());
                chatListItemSet.add(chatListItem);
            }
        }
        // sort by AbsTime 降序
        List<ChatListItem> chatListItems = new java.util.ArrayList<>(chatListItemSet);
        chatListItems.sort((o1, o2) -> (int) (o2.getAbsTime() - o1.getAbsTime()));
        return chatListItems;
    }

    public void updateAllMessageRead(Integer userId, Integer friendId) {
        String tableName = getTableName(friendId, userId);
        chatDao.updateAllMessageRead(tableName);
    }

    public void sendMessageToServer(Integer userId, Integer friendId, String content, String messageType, String token, String path) throws IOException {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(userId);
        chatMessage.setReceiverId(friendId);
        chatMessage.setReceiverType("user");
        chatMessage.setContent(content);
        chatMessage.setMessageType(messageType);
        chatMessage.setFilePath(path);
        chatMessage.setSendTime(new java.sql.Timestamp(System.currentTimeMillis()));
        chatMessage.setIsRead(true);
        chatMessage.setIsDownload(true);
        String tableName = getTableName(friendId, userId);
        try {
            sendMessage(userId.toString(), friendId.toString(), "0", "user", content, messageType, token, path);
            if (messageType.equals("file")) {
                content = content + ":" + new File(path).length();
                chatMessage.setContent(content);
                String fileName = new File(path).getName();
                chatMessage.setFileName(fileName);
            }
            insertMessage(tableName, chatMessage);
        } catch (IOException e) {
            Logger.e("ChatRepository", "Failed to send message", e);
            throw e;
        }
        Friend friend = contactRepository.getFriendByFriendId(friendId);
        friend.setMessageTime(chatMessage.getSendTime());
        contactRepository.updateFriend(friend);
    }

    // TODO
    public void sendMessageToServer(Integer userId, Integer otherId, Integer groupId, String content, String messageType, String token, String path) throws IOException {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(userId);
        chatMessage.setGroupId(groupId);
        chatMessage.setReceiverType("group");
        chatMessage.setContent(content);
        chatMessage.setMessageType(messageType);
        chatMessage.setFilePath(path);
        chatMessage.setSendTime(new java.sql.Timestamp(System.currentTimeMillis()));
        chatMessage.setIsRead(true);
        chatMessage.setIsDownload(true);
        String tableName = getTableName(groupId);
        insertMessage(tableName, chatMessage);
        sendMessage(userId.toString(), "0", groupId.toString(), "group", content, messageType, token, path);
    }

    public List<MessageListItem> updateMessagesFromLocal(Integer userId, Integer friendId, String token) throws IOException {
        String tableName = getTableName(friendId, userId);
        List<ChatMessage> messages = getMessages(tableName);
        List<MessageListItem> messageListItems = new ArrayList<>();
        for (ChatMessage message : messages) {
            MessageListItem messageListItem = new MessageListItem();
            if (message.getMessageType().equals("file")) {
                messageListItem.setFileName(message.getFileName());
                String content = message.getContent();
                String[] split = content.split(":");
                Long fileSize = Long.parseLong(split[split.length - 1]);
                messageListItem.setFileSize(fileSize);
            } else {
                messageListItem.setFileName(null);
                messageListItem.setFileSize(0L);
            }
            messageListItem.setId(message.getMessageId());
            messageListItem.setContent(message.getContent());
            messageListItem.setMessageType(message.getMessageType());
            messageListItem.setFilePath(message.getFilePath());
            messageListItem.setMe(message.getSenderId().equals(userId));
            messageListItem.setSendTime(message.getSendTime());
            messageListItems.add(messageListItem);
        }
        return messageListItems;
    }

    public List<GroupMessageListItem> updateGroupMessagesFromLocal(Integer userId, Integer groupId, String token) throws IOException {
        String tableName = getTableName(groupId);
        List<ChatMessage> messages = getMessages(tableName);
        List<GroupMessageListItem> messageListItems = new ArrayList<>();
        for (ChatMessage message : messages) {
            GroupMessageListItem messageListItem = new GroupMessageListItem();
            if (message.getMessageType().equals("file")) {
                messageListItem.setFileName(message.getFileName());
                String content = message.getContent();
                String[] split = content.split(":");
                Long fileSize = Long.parseLong(split[split.length - 1]);
                messageListItem.setFileSize(fileSize);
            } else {
                messageListItem.setFileName(null);
                messageListItem.setFileSize(0L);
            }
            messageListItem.setId(message.getMessageId());
            messageListItem.setSenderId(message.getSenderId());
            messageListItem.setContent(message.getContent());
            messageListItem.setMessageType(message.getMessageType());
            messageListItem.setFilePath(message.getFilePath());
//            messageListItem.setMe(message.getSenderId().equals(userId));
            messageListItem.setSendTime(message.getSendTime());

            UserProfile userProfile = userRepository.getUserProfileById(message.getSenderId());
            messageListItem.setSenderName(userProfile.getNickname());
            messageListItems.add(messageListItem);
        }
        return messageListItems;
    }

    public void setAllMessagesRead(Integer userId, Integer friendId) {
        String tableName = getTableName(friendId, userId);
        chatDao.updateAllMessageRead(tableName);
    }

    public Map<String, String> getFileInfo(String userId, String messageId, String token) throws IOException {
        ResultModel<Map<String, String>> res = chatService.getFileInfo(userId, messageId, token).execute().body();
        return NetException.responseCheck(res);
    }
}
