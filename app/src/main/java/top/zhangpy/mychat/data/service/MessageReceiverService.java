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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import top.zhangpy.mychat.R;

public class MessageReceiverService extends Service {
    private static final String CHANNEL_ID = "MessageServiceChannel";
    private static final String WEBSOCKET_URL = "ws://" + SERVER_IP + ":8081/ws";
    private WebSocket webSocket;
    private OkHttpClient client;

    private final BroadcastReceiver serviceStoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.MESSAGE_HANDLER_SERVICE_STOPPED".equals(intent.getAction())) {
                Log.i("MessageReceiverService", "MessageHandlerService stopped, restarting...");
                restartMessageHandlerService();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification("Connecting to server..."));

        connectWebSocket();

        // 注册广播接收器监听 MessageHandlerService 停止事件
        IntentFilter filter = new IntentFilter("top.zhangpy.mychat.MESSAGE_HANDLER_SERVICE_STOPPED");
        registerReceiver(serviceStoppedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
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
                new Handler(getMainLooper()).post(() -> broadcastMessage(text));
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

    private void broadcastMessage(String message) {
        Intent intent = new Intent("top.zhangpy.mychat.MESSAGE_RECEIVED");
        intent.putExtra("message", message);
        Log.i("MessageReceiverService", "Broadcasting message: " + message);
        sendBroadcast(intent);
    }

    private Notification getNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Message Service")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification) // 替换为您的通知图标
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String contentText) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, getNotification(contentText));
        }
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

    private void restartMessageHandlerService() {
        Intent intent = new Intent(this, MessageHandlerService.class);
        startService(intent);
    }

    private String loadToken() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Service destroyed");
        }
        unregisterReceiver(serviceStoppedReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
