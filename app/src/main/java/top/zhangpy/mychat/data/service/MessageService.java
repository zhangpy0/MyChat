package top.zhangpy.mychat.data.service;

import static top.zhangpy.mychat.util.Constants.SERVER_IP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import top.zhangpy.mychat.R;
import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.mapper.ChatMessageMapper;
import top.zhangpy.mychat.data.mapper.ServerMessageMapper;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ServerMessageModel;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.ContactRepository;

public class MessageService extends Service {

    private static final String CHANNEL_ID = "MessageServiceChannel";
    private static final String WEBSOCKET_URL = "ws://" + SERVER_IP + ":8081/ws";

    private static final String TAG = "MessageService";
    private WebSocket webSocket;
    private OkHttpClient client;

    private NotificationManager notificationManager;
    private ContactRepository contactRepository;
    private ChatRepository chatRepository;
    private Integer userId;
    private String token;

    private int totalUnreadMessages = 0;
    private int contactId = -1;
    private final Set<Integer> unreadMessageSenders = new HashSet<>();

    private final BroadcastReceiver clearNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.CLEAR_NOTIFICATIONS".equals(intent.getAction())) {
                clearNotifications();
            }
        }
    };

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

        startForeground(1, getNotification("Connecting to server..."));

        // 开始 WebSocket 连接
        connectWebSocket();
        // 初始化通知管理
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        createNotificationChannelForNew();

        // 注册消息接收器（广播接收器）
        IntentFilter clearFilter = new IntentFilter("top.zhangpy.mychat.CLEAR_NOTIFICATIONS");
        registerReceiver(clearNotificationReceiver, clearFilter, Context.RECEIVER_NOT_EXPORTED);

        updateNotificationForNew();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Integer loadUserId() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    public String loadToken() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    public void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Message Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Service destroyed");
        }
        unregisterReceiver(clearNotificationReceiver);
        Log.i(TAG, "MessageService stopped and receiver unregistered");

    }

    private Notification getNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Message Service")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void updateNotification(String contentText) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, getNotification(contentText));
        }
    }

    private void connectWebSocket() {
        String token = loadToken();
        if (token == null) {
            Log.e("MessageReceiverService", "No token found");
            stopSelf(); // 没有 token，停止服务
            return;
        }

        client = new OkHttpClient.Builder()
                .pingInterval(5, java.util.concurrent.TimeUnit.SECONDS) // 心跳间隔
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Log.d("OkHttp", "Request: " + request.headers());
                    return chain.proceed(request);
                })
                .build();

        Request request = new Request.Builder()
                .url(WEBSOCKET_URL)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .addHeader("token", token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                updateNotification("Connected to server");
                Log.i("MessageReceiverService", "Connected to server");
                sendHeartbeat();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.i("MessageReceiverService", "Received message: " + text);
                webSocket.send("get");
                handleMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Log.e("MessageReceiverService", "WebSocket failure: " + t.getMessage());
                reconnectWebSocket();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.i("MessageReceiverService", "WebSocket closed: " + reason);
                reconnectWebSocket();
            }
        });

        client.dispatcher().executorService().shutdown();
    }

    private void sendHeartbeat() {
        ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        heartbeatScheduler.scheduleWithFixedDelay(() -> {
            if (webSocket != null) {
                try {
                    webSocket.send("heartbeat");
                    Log.i("MessageReceiverService", "Sent heartbeat");
                } catch (Exception e) {
                    Log.e("MessageReceiverService", "Heartbeat failed: " + e.getMessage());
                }
            }
        }, 0, 5, TimeUnit.SECONDS); // 每5秒发送一次
    }

    private void reconnectWebSocket() {
        Log.i("MessageReceiverService", "Reconnecting to server...");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(3000); // 延迟重连
                connectWebSocket();
            } catch (InterruptedException e) {
                Log.e("MessageReceiverService", "Reconnect interrupted");
            }
        });
    }

    // handle message
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

    private void processMessage(String message) {
        Log.i(TAG, "Handling message: " + message);

        contactId = -1;

        ServerMessageModel serverMessage = ServerMessageMapper.mapToServerMessageModel(message);
        if (serverMessage == null) {
            Log.e(TAG, "Failed to parse message: " + message);
            return;
        }
        boolean isServerMessage = ServerMessageMapper.isServerMessage(serverMessage);
        updateNotificationForNew(String.valueOf(serverMessage.getSenderId()));
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

    private void updateNotificationForNew() {
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

    private void updateNotificationForNew(String senderId) {
        totalUnreadMessages++;
        unreadMessageSenders.add(Integer.parseInt(senderId));
        String contentText = unreadMessageSenders.size() + "个联系人发来了" + totalUnreadMessages + "条消息";

        Notification notification = new NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("新消息")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(1, notification);
    }

    private void createNotificationChannelForNew() {
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
