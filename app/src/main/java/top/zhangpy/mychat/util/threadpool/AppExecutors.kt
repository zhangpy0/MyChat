package top.zhangpy.mychat.util.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import top.zhangpy.mychat.util.Logger;

public class AppExecutors {
    // 单例模式
    private static final AppExecutors INSTANCE = new AppExecutors();

    // 针对不同任务类型的线程池
    private final ExecutorService networkIO;     // 网络请求线程池
    private final ExecutorService databaseIO;    // 数据库操作线程池
    private final ExecutorService serialExecutor;// 顺序执行线程池
    private final ScheduledExecutorService scheduledExecutor; // 定时任务

    private AppExecutors() {
        // 网络请求线程池 (IO密集型)
        networkIO = new ThreadPoolExecutor(
                4,                                     // 核心线程数
                16,                                    // 最大线程数
                60L, TimeUnit.SECONDS,                 // 空闲线程存活时间
                new LinkedBlockingQueue<>(256),        // 任务队列
                new CustomThreadFactory("Network"),    // 自定义线程工厂
                new CustomRejectedPolicy()            // 自定义拒绝策略
        );

        // 数据库操作线程池 (建议单线程)
        databaseIO = Executors.newSingleThreadExecutor(
                new CustomThreadFactory("Database")
        );

        // 顺序执行线程池 (用于需要严格顺序的任务)
        serialExecutor = Executors.newSingleThreadExecutor(
                new CustomThreadFactory("Serial")
        );

        // 定时任务线程池
        scheduledExecutor = Executors.newScheduledThreadPool(
                2,
                new CustomThreadFactory("Scheduled")
        );
    }

    public static AppExecutors get() {
        return INSTANCE;
    }

    // 自定义线程工厂
    private static class CustomThreadFactory implements ThreadFactory {
        private final String poolName;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, "Pool-" + poolName + "-Thread-" + threadNumber.getAndIncrement());
        }
    }

    // 自定义拒绝策略
    private static class CustomRejectedPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 记录日志或持久化任务
            Logger.w("ThreadPool", "Task rejected, saving to disk: " + r);

        }
    }

    // 对外暴露接口
    public ExecutorService networkIO() { return networkIO; }
    public ExecutorService databaseIO() { return databaseIO; }
    public ExecutorService serial() { return serialExecutor; }
    public ScheduledExecutorService scheduled() { return scheduledExecutor; }

    public void shutdown() {
        networkIO.shutdown();
        databaseIO.shutdown();
        serialExecutor.shutdown();
        scheduledExecutor.shutdown();

        try {
            if (!networkIO.awaitTermination(5, TimeUnit.SECONDS)) {
                networkIO.shutdownNow();
            }
            if (!databaseIO.awaitTermination(5, TimeUnit.SECONDS)) {
                databaseIO.shutdownNow();
            }
            if (!serialExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                serialExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}