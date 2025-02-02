package top.zhangpy.mychat.data.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.util.Logger;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    public static final String EXTRA_CONTACT_ID = "contact_id";

    private ChatRepository chatRepository;

    public DownloadService() {
        super("ImageDownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        Logger.initialize(context);
        Logger.enableLogging(true);
        chatRepository = new ChatRepository(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Logger.e(TAG, "Received null intent");
            return;
        }

        int contactId = intent.getIntExtra(EXTRA_CONTACT_ID, -1);
        if (contactId == -1) {
            Logger.e(TAG, "Invalid contact ID provided");
            return;
        }

        // 获取当前用户信息
        int currentUserId = loadUserId();
        String token = loadToken();
        if (currentUserId == 0 || token == null) {
            Logger.e(TAG, "User not authenticated");
            return;
        }

        String tableName = ChatRepository.getTableName(contactId, currentUserId);

        List<ChatMessage> allMessages = new ArrayList<>();

        addMessagesFromTable(allMessages, tableName);

        // 处理未下载的图片消息
        for (ChatMessage message : allMessages) {
            if (isImagePendingDownload(message)) {
                downloadImage(message, currentUserId, token);
            }
        }

        notifyUIUpdate(contactId);
    }

    private void addMessagesFromTable(List<ChatMessage> messages, String tableName) {
        if (chatRepository.isTableExist(tableName)) {
            List<ChatMessage> tableMessages = chatRepository.getMessages(tableName);
            if (tableMessages != null) {
                messages.addAll(tableMessages);
            }
        }
    }

    private boolean isImagePendingDownload(ChatMessage message) {
        return "image".equals(message.getMessageType()) && !message.getIsDownload();
    }

    private void downloadImage(ChatMessage message, int currentUserId, String token) {
        try {
            chatRepository.downloadImage(
                    getApplicationContext(),
                    String.valueOf(currentUserId),
                    token,
                    message
            );
            Logger.d(TAG, "Successfully downloaded image for message: " + message.getMessageId());
        } catch (IOException e) {
            Logger.e(TAG, "Failed to download image for message: " + message.getMessageId(), e);
        }
    }

    // 启动Service的辅助方法
    public static void startService(Context context, int contactId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(EXTRA_CONTACT_ID, contactId);
        context.startService(intent);
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    private void notifyUIUpdate(Integer contactId) {
        Intent intent = new Intent("top.zhangpy.mychat.UPDATE_MESSAGES")
                .putExtra("contact_id", contactId);
        sendBroadcast(intent);
    }
}
