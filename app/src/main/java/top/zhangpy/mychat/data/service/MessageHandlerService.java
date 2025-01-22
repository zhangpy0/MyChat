package top.zhangpy.mychat.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.mapper.ChatMessageMapper;
import top.zhangpy.mychat.data.mapper.ServerMessageMapper;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ServerMessageModel;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.ContactRepository;

public class MessageHandlerService extends Service {
    private static final String TAG = "MessageHandlerService";

    private NotificationManager notificationManager;

    private ContactRepository contactRepository;

    private ChatRepository chatRepository;

    private Integer userId;

    private String token;

    private int totalUnreadMessages = 0;

    private int contactId = -1;

    private final Set<Integer> unreadMessageSenders = new HashSet<>();

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.MESSAGE_RECEIVED".equals(intent.getAction())) {
                String message = intent.getStringExtra("message");
                if (message != null) {
                    handleMessage(message);
                }
            }
        }
    };

    private final BroadcastReceiver clearNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.CLEAR_NOTIFICATIONS".equals(intent.getAction())) {
                clearNotifications();
            }
        }
    };

//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {
        super.onCreate();
        userId = loadUserId();
        token = loadToken();
        contactRepository = new ContactRepository(getApplication());
        chatRepository = new ChatRepository(getApplication());
        if (userId == -1 || token == null) {
            Log.e(TAG, "No user id or token found");
            stopSelf();
            return;
        }
        IntentFilter filter = new IntentFilter("top.zhangpy.mychat.MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(messageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(messageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
        Log.i(TAG, "MessageHandlerService started and receiver registered");

        IntentFilter clearFilter = new IntentFilter("top.zhangpy.mychat.CLEAR_NOTIFICATIONS");
        registerReceiver(clearNotificationReceiver, clearFilter, Context.RECEIVER_NOT_EXPORTED);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        updateNotification();
    }

    private void handleMessage(String message){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                processMessage(message);
                notifyUIUpdate();
            } catch (Exception e) {
                Log.e(TAG, "Error processing message: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        unregisterReceiver(clearNotificationReceiver);
        Log.i(TAG, "MessageHandlerService stopped and receiver unregistered");

        // 广播 MessageHandlerService 被停止的事件
        Intent intent = new Intent("top.zhangpy.mychat.MESSAGE_HANDLER_SERVICE_STOPPED");
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    private void processMessage(String message) {
        Log.i(TAG, "Handling message: " + message);

        contactId = -1;

        ServerMessageModel serverMessage = ServerMessageMapper.mapToServerMessageModel(message);
        if (serverMessage == null) {
            Log.e(TAG, "Failed to parse message: " + message);
            return;
        }
        boolean isServerMessage = ServerMessageMapper.isServerMessage(serverMessage);
        updateNotification(String.valueOf(serverMessage.getSenderId()));
        if (isServerMessage) {
            Log.i(TAG, "Received server message: " + serverMessage);
            int messageType = ServerMessageMapper.getServerMessageType(serverMessage);
            RequestMapModel requestMap = new RequestMapModel();
            requestMap.setUserId(String.valueOf(userId));

            switch (messageType) {
                case 1:
                    // update contactApply
                    try {
                        contactRepository.updateContactApplyFromServer(token, requestMap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to update contactApply: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                    break;
                case 2:
                    // update contactApply and contact(Friend)
                    try {
                        contactRepository.updateContactApplyFromServer(token, requestMap);
                        requestMap = new RequestMapModel();
                        requestMap.setUserId(String.valueOf(userId));
                        contactRepository.updateContactOfFriend(token, requestMap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to update contactApply and contact(Friend): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                    break;
                case 3:
                    // update contactApply and contact(Group)
                    try {
                        contactRepository.updateContactApplyFromServer(token, requestMap);
                        requestMap = new RequestMapModel();
                        requestMap.setUserId(String.valueOf(userId));
                        contactRepository.updateContactOfGroup(token, requestMap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to update contactApply and contact(Group): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                    break;
                case 4:
                    // update contact(Group)
                    try {
                        contactRepository.updateContactOfGroup(token, requestMap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to update contact(Group): " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    Log.e(TAG, "Invalid message type: " + messageType);
            }
        } else {
            Log.i(TAG, "Received chat message: " + serverMessage);
            ChatMessageModel chatMessage = ServerMessageMapper.mapToChatMessageModel(serverMessage);
            ChatMessage chatMessageEntity = ChatMessageMapper.mapToChatMessage(chatMessage);

            String tableName = ChatRepository.getTableName(chatMessageEntity);
            chatRepository.createChatTable(tableName);
            try {
                boolean isUpdated = chatRepository.updateChatFromServer(getApplicationContext(), String.valueOf(userId), token, chatMessageEntity);
                if (isUpdated) {
                    Log.i(TAG, "Chat updated from server: " + chatMessageEntity);
                } else {
                    Log.e(TAG, "Failed to update chat from server: " + chatMessageEntity);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to update chat from server: " + e.getMessage());
            }
            if (chatMessageEntity.getReceiverType().equals("user")) {
                contactId = chatMessageEntity.getSenderId();
            } else {
                contactId = chatMessageEntity.getGroupId();
            }
        }
    }

    private void notifyUIUpdate() {
        Intent intent = new Intent("top.zhangpy.mychat.UPDATE_MESSAGES")
                .putExtra("contact_id", contactId);
        sendBroadcast(intent);
    }


    private void updateNotification(String senderId) {
        totalUnreadMessages++;
        unreadMessageSenders.add(Integer.parseInt(senderId));
        String contentText = unreadMessageSenders.size() + "个联系人发来了" + totalUnreadMessages + "条消息";

        Notification notification = new NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.ic_notification) // 替换为实际的图标
                .setContentTitle("新消息")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void updateNotification() {
        String contentText = "运行中";

        Notification notification = new NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.ic_notification) // 替换为实际的图标
                .setContentTitle("消息通知")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification);
        notificationManager.notify(1, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "MESSAGE_CHANNEL",
                "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for new messages");
        notificationManager.createNotificationChannel(channel);
    }

    private void clearNotifications() {
        // 重置未读消息数，并移除通知
        totalUnreadMessages = 0;
        unreadMessageSenders.clear();
        notificationManager.cancel(1); // 1 是通知的 ID
        Log.i(TAG, "Notifications cleared and unread message count reset");
    }
}
