package top.zhangpy.mychat.data.service

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import top.zhangpy.mychat.data.service.MessageService

class ServiceCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {

        val intent = Intent(applicationContext, MessageService::class.java)
        applicationContext.startForegroundService(intent)
        return Result.success()
    }
}