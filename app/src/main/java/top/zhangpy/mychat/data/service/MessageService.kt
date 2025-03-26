package top.zhangpy.mychat.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import top.zhangpy.mychat.R
import top.zhangpy.mychat.data.mapper.ChatMessageMapper
import top.zhangpy.mychat.data.mapper.ServerMessageMapper
import top.zhangpy.mychat.data.remote.model.RequestMapModel
import top.zhangpy.mychat.data.repository.ChatRepository
import top.zhangpy.mychat.data.repository.ContactRepository
import top.zhangpy.mychat.ui.view.AppStartActivity
import top.zhangpy.mychat.util.Constants.SERVER_IP
import top.zhangpy.mychat.util.Logger
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MessageService : Service() {

    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var contactRepository: ContactRepository
    private lateinit var chatRepository: ChatRepository
    private var userId: Int = -1
    private var token: String? = null

    private var totalUnreadMessages = 0
    private var contactId = -1
    private val unreadMessageSenders = mutableSetOf<Int>()

    private val clearNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "top.zhangpy.mychat.CLEAR_NOTIFICATIONS") {
                clearNotifications()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        userId = loadUserId()
        token = loadToken()
        contactRepository = ContactRepository(application)
        chatRepository = ChatRepository(application)

        Logger.initialize(applicationContext)
        Logger.enableLogging(true)

        if (userId == -1 || token == null) {
            Logger.e(TAG, "No user id or token found")
            stopSelf()
            return
        }

        startForeground(1, getNotification("已连接服务器"))

        connectWebSocket()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        createNotificationChannelForNew()

        val clearFilter = IntentFilter("top.zhangpy.mychat.CLEAR_NOTIFICATIONS")
        registerReceiver(clearNotificationReceiver, clearFilter, Context.RECEIVER_NOT_EXPORTED)

        updateNotificationForNew()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartService = Intent(this, MessageService::class.java).apply {
            `package` = packageName
        }
        startService(restartService)
        super.onTaskRemoved(rootIntent)
    }

    private fun loadUserId(): Int {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }

    private fun loadToken(): String? {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Message Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Service destroyed")
        unregisterReceiver(clearNotificationReceiver)
        Logger.i(TAG, "MessageService stopped and receiver unregistered")
    }

    private fun getNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("消息服务")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(contentText: String) {
        notificationManager.notify(1, getNotification(contentText))
    }

    private fun connectWebSocket() {
        val token = loadToken() ?: run {
            Logger.e("MessageReceiverService", "No token found")
            stopSelf()
            return
        }

        client = OkHttpClient.Builder()
            .pingInterval(5, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                Logger.d("OkHttp", "Request: ${request.headers}")
                chain.proceed(request)
            }
            .build()

        val request = Request.Builder()
            .url(WEBSOCKET_URL)
            .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            .addHeader("token", token)
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                updateNotification("Connected to server")
                Logger.i("MessageReceiverService", "Connected to server")
                sendHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Logger.i("MessageReceiverService", "Received message: $text")
                webSocket.send("get")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Logger.e("MessageReceiverService", "WebSocket failure: ${t.message}")
                reconnectWebSocket()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Logger.i("MessageReceiverService", "WebSocket closed: $reason")
                reconnectWebSocket()
            }
        })

        client?.dispatcher?.executorService?.shutdown()
    }

    private fun sendHeartbeat() {
        val heartbeatScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        heartbeatScheduler.scheduleWithFixedDelay({
            webSocket?.let {
                try {
                    it.send("heartbeat")
                    Logger.i("MessageReceiverService", "Sent heartbeat")
                } catch (e: Exception) {
                    Logger.e("MessageReceiverService", "Heartbeat failed: ${e.message}")
                }
            }
        }, 0, 5, TimeUnit.SECONDS)
    }

    private fun reconnectWebSocket() {
        Logger.i("MessageReceiverService", "Reconnecting to server...")
        Executors.newSingleThreadExecutor().execute {
            try {
                Thread.sleep(3000)
                connectWebSocket()
            } catch (e: InterruptedException) {
                Logger.e("MessageReceiverService", "Reconnect interrupted")
            }
        }
    }

    private fun handleMessage(message: String) {
        Executors.newSingleThreadExecutor().execute {
            try {
                processMessage(message)
                notifyUIUpdate()
            } catch (e: Exception) {
                Logger.e(TAG, "Error processing message: ${e.message}")
            }
        }
    }

    private fun processMessage(message: String) {
        Logger.i(TAG, "Handling message: $message")

        contactId = -1

        val serverMessage = ServerMessageMapper.mapToServerMessageModel(message)
            ?: run {
                Logger.e(TAG, "Failed to parse message: $message")
                return
            }

        val isServerMessage = ServerMessageMapper.isServerMessage(serverMessage)
        updateNotificationForNew(serverMessage.senderId.toString())

        if (isServerMessage) {
            Logger.i(TAG, "Received server message: $serverMessage")
            val messageType = ServerMessageMapper.getServerMessageType(serverMessage)
            val requestMap = RequestMapModel().apply { userId = this@MessageService.userId.toString() }

            when (messageType) {
                1 -> try {
                    contactRepository.updateContactApplyFromServer(token!!, requestMap)
                } catch (e: IOException) {
                    Logger.e(TAG, "Failed to update contactApply: ${e.message}")
                    throw RuntimeException(e)
                }
                2 -> try {
                    contactRepository.updateContactApplyFromServer(token!!, requestMap)
                    contactRepository.updateContactOfFriend(token!!, RequestMapModel().apply { userId = this@MessageService.userId.toString() })
                } catch (e: IOException) {
                    Logger.e(TAG, "Failed to update contactApply and contact(Friend): ${e.message}")
                    throw RuntimeException(e)
                }
                3 -> try {
                    contactRepository.updateContactApplyFromServer(token!!, requestMap)
                    contactRepository.updateContactOfGroup(token!!, RequestMapModel().apply { userId = this@MessageService.userId.toString() })
                } catch (e: IOException) {
                    Logger.e(TAG, "Failed to update contactApply and contact(Group): ${e.message}")
                    throw RuntimeException(e)
                }
                4 -> try {
                    contactRepository.updateContactOfGroup(token!!, requestMap)
                } catch (e: IOException) {
                    Logger.e(TAG, "Failed to update contact(Group): ${e.message}")
                    throw RuntimeException(e)
                }
                else -> Logger.e(TAG, "Invalid message type: $messageType")
            }
        } else {
            Logger.i(TAG, "Received chat message: $serverMessage")
            val chatMessage = ServerMessageMapper.mapToChatMessageModel(serverMessage)
            val chatMessageEntity = ChatMessageMapper.mapToChatMessage(chatMessage)

            val tableName = ChatRepository.getTableName(chatMessageEntity)
            chatRepository.createChatTable(tableName)
            try {
                val isUpdated = chatRepository.updateChatFromServer(
                    applicationContext,
                    userId.toString(),
                    token!!,
                    chatMessageEntity
                )
                if (isUpdated) {
                    Logger.i(TAG, "Chat updated from server: $chatMessageEntity")
                } else {
                    Logger.e(TAG, "Failed to update chat from server: $chatMessageEntity")
                }
            } catch (e: IOException) {
                Logger.e(TAG, "Failed to update chat from server: ${e.message}")
            }
            contactId = if (chatMessageEntity.receiverType == "user") {
                chatMessageEntity.senderId
            } else {
                chatMessageEntity.groupId
            }
        }
    }

    private fun notifyUIUpdate() {
        sendBroadcast(Intent("top.zhangpy.mychat.UPDATE_MESSAGES").apply {
            putExtra("contact_id", contactId)
        })
    }

    private fun updateNotificationForNew() {
        val notification = NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("消息通知")
            .setContentText("运行中")
            .setStyle(NotificationCompat.BigTextStyle().bigText("运行中"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        startForeground(1, notification)
        notificationManager.notify(1, notification)
    }

    private fun updateNotificationForNew(senderId: String) {
        totalUnreadMessages++
        unreadMessageSenders.add(senderId.toInt())
        val contentText = "${unreadMessageSenders.size}个联系人发来了${totalUnreadMessages}条消息"

        val fullScreenIntent = Intent(this, AppStartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("新消息")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setLights(Color.BLUE, 500, 2000)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_open, "立即查看", fullScreenPendingIntent)
            .setTimeoutAfter(3000)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannelForNew() {
        val channel = NotificationChannel(
            "MESSAGE_CHANNEL",
            "Message Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new messages"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun clearNotifications() {
        totalUnreadMessages = 0
        unreadMessageSenders.clear()
        notificationManager.cancel(1)
        Logger.i(TAG, "Notifications cleared and unread message count reset")
    }

    companion object {
        private const val CHANNEL_ID = "MessageServiceChannel"
        private const val WEBSOCKET_URL = "ws://$SERVER_IP:8081/ws"
        private const val TAG = "MessageService"
    }
}