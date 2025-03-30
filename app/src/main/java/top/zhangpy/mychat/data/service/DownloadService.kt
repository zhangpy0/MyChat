package top.zhangpy.mychat.data.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.zhangpy.mychat.data.local.entity.ChatMessage
import top.zhangpy.mychat.data.repository.ChatRepository
import top.zhangpy.mychat.util.Logger
import top.zhangpy.mychat.util.threadpool.AppExecutors
import java.io.IOException

class DownloadService : IntentService("ImageDownloadService") {
    private var chatRepository: ChatRepository? = null

    override fun onCreate() {
        super.onCreate()
        val context = applicationContext
        Logger.initialize(context)
        Logger.enableLogging(true)
        chatRepository = ChatRepository(context)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            Logger.e(TAG, "Received null intent")
            return
        }

        val contactId = intent.getIntExtra(EXTRA_CONTACT_ID, -1)
        if (contactId == -1) {
            Logger.e(TAG, "Invalid contact ID provided")
            return
        }

        // 获取当前用户信息
        val currentUserId = loadUserId()
        val token = loadToken()
        if (currentUserId == 0 || token == null) {
            Logger.e(TAG, "User not authenticated")
            return
        }

        val appExecutors = AppExecutors.get()
        val coroutineScope: CoroutineScope = appExecutors.databaseIODispatch() as CoroutineScope

        val tableName = ChatRepository.getTableName(contactId, currentUserId)

        val allMessages: MutableList<ChatMessage> = ArrayList()

        coroutineScope.launch() {
            addMessagesFromTable(allMessages, tableName)
            allMessages.forEach { message ->
                if (isImagePendingDownload(message)) {
                    launch(appExecutors.networkIODispatch()) {
                        downloadImage(message, currentUserId, token)
                    }
                }
            }
        }

//        addMessagesFromTable(allMessages, tableName)

        // 处理未下载的图片消息
//        for (message in allMessages) {
//            if (isImagePendingDownload(message)) {
//                downloadImage(message, currentUserId, token)
//            }
//        }

        notifyUIUpdate(contactId)
    }

    private fun addMessagesFromTable(messages: MutableList<ChatMessage>, tableName: String) {
        if (chatRepository!!.isTableExist(tableName)) {
            val tableMessages = chatRepository!!.getMessages(tableName)
            if (tableMessages != null) {
                messages.addAll(tableMessages)
            }
        }
    }

    private fun isImagePendingDownload(message: ChatMessage): Boolean {
        return "image" == message.messageType && !message.isDownload
    }

    private fun downloadImage(message: ChatMessage, currentUserId: Int, token: String) {
        try {
            chatRepository!!.downloadImage(
                applicationContext,
                currentUserId.toString(),
                token,
                message
            )
            Logger.d(TAG, "Successfully downloaded image for message: " + message.messageId)
        } catch (e: IOException) {
            Logger.e(TAG, "Failed to download image for message: " + message.messageId, e)
        }
    }

    private fun loadUserId(): Int {
        val prefs = applicationContext.getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }

    private fun loadToken(): String? {
        val prefs = applicationContext.getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    private fun notifyUIUpdate(contactId: Int) {
        val intent = Intent("top.zhangpy.mychat.UPDATE_MESSAGES")
            .putExtra("contact_id", contactId)
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "DownloadService"
        const val EXTRA_CONTACT_ID: String = "contact_id"

        // 启动Service的辅助方法
        @JvmStatic
        fun startService(context: Context, contactId: Int) {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(EXTRA_CONTACT_ID, contactId)
            context.startService(intent)
        }
    }
}
