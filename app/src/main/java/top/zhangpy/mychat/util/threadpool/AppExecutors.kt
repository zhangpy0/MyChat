package top.zhangpy.mychat.util.threadpool

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import top.zhangpy.mychat.util.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AppExecutors private constructor() {
    // 针对不同任务类型的线程池
    private val networkIO: ExecutorService // 网络请求线程池
    private val databaseIO: ExecutorService // 数据库操作线程池
    private val serialExecutor: ExecutorService // 顺序执行线程池
    private val scheduledExecutor: ScheduledExecutorService // 定时任务

    init {
        // 网络请求线程池 (IO密集型)
        networkIO = ThreadPoolExecutor(
            4,  // 核心线程数
            16,  // 最大线程数
            60L, TimeUnit.SECONDS,  // 空闲线程存活时间
            LinkedBlockingQueue(256),  // 任务队列
            CustomThreadFactory("Network"),  // 自定义线程工厂
            CustomRejectedPolicy() // 自定义拒绝策略
        )

        // 数据库操作线程池 (建议单线程)
        databaseIO = Executors.newSingleThreadExecutor(
            CustomThreadFactory("Database")
        )

        // 顺序执行线程池 (用于需要严格顺序的任务)
        serialExecutor = Executors.newSingleThreadExecutor(
            CustomThreadFactory("Serial")
        )

        // 定时任务线程池
        scheduledExecutor = Executors.newScheduledThreadPool(
            2,
            CustomThreadFactory("Scheduled")
        )
    }

    // 自定义线程工厂
    private class CustomThreadFactory(private val poolName: String) : ThreadFactory {
        private val threadNumber = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread(r, "Pool-" + poolName + "-Thread-" + threadNumber.getAndIncrement())
        }
    }

    // 自定义拒绝策略
    private class CustomRejectedPolicy : RejectedExecutionHandler {
        override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
            // 记录日志或持久化任务
            Logger.w("ThreadPool", "Task rejected, saving to disk: $r")
        }
    }

    // 对外暴露接口
    fun networkIO(): ExecutorService {
        return networkIO
    }

    fun databaseIO(): ExecutorService {
        return databaseIO
    }

    fun serial(): ExecutorService {
        return serialExecutor
    }

    fun scheduled(): ScheduledExecutorService {
        return scheduledExecutor
    }

    fun networkIODispatch() : ExecutorCoroutineDispatcher {
        return networkIO.asCoroutineDispatcher()
    }

    fun databaseIODispatch() : ExecutorCoroutineDispatcher {
        return databaseIO.asCoroutineDispatcher()
    }

    fun serialDispatch() : ExecutorCoroutineDispatcher {
        return serialExecutor.asCoroutineDispatcher()
    }

    fun scheduledDispatch() : ExecutorCoroutineDispatcher {
        return scheduledExecutor.asCoroutineDispatcher()
    }

    fun shutdown() {
        networkIO.shutdown()
        databaseIO.shutdown()
        serialExecutor.shutdown()
        scheduledExecutor.shutdown()

        try {
            if (!networkIO.awaitTermination(5, TimeUnit.SECONDS)) {
                networkIO.shutdownNow()
            }
            if (!databaseIO.awaitTermination(5, TimeUnit.SECONDS)) {
                databaseIO.shutdownNow()
            }
            if (!serialExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                serialExecutor.shutdownNow()
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        // 单例模式
        private val INSTANCE = AppExecutors()

        fun get(): AppExecutors {
            return INSTANCE
        }
    }
}