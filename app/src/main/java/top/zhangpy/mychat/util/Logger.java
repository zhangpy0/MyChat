package top.zhangpy.mychat.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.BuildConfig;

public class Logger {
    private static final String TAG = "Logger";
    private static final int MAX_LOG_FILES = 7;
    private static final String LOG_DIR_NAME = "app_logs";
    private static final String LOG_FILE_PREFIX = "log_";
    private static final String LOG_FILE_EXT = ".txt";

    private static volatile Logger instance;
    private final ExecutorService logExecutor;
    private final SimpleDateFormat logDateFormat;
    private final SimpleDateFormat fileDateFormat;

    private static Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private File logDirectory;
    private boolean isEnabled = true;

    private Logger(Context context) {
        logExecutor = Executors.newSingleThreadExecutor();
        logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        fileDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        initializeLogDirectory(context);
        initGlobalExceptionHandler();
    }

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new Logger(context.getApplicationContext());
        }
    }

    public static void initGlobalExceptionHandler() {
        if (defaultExceptionHandler == null) {
            defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                // 记录崩溃信息
                String crashLog = buildCrashReport(throwable);
                logToFile(Log.ERROR, "CRASH", crashLog, throwable);

                // 执行原有默认处理器
                if (defaultExceptionHandler != null) {
                    defaultExceptionHandler.uncaughtException(thread, throwable);
                }
            });
        }
    }

    public static void enableLogging(boolean enabled) {
        if (instance != null) {
            instance.isEnabled = enabled;
        }
    }

    private void initializeLogDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logDirectory = new File(context.getExternalFilesDir(null), LOG_DIR_NAME);
        } else {
            logDirectory = new File(context.getFilesDir(), LOG_DIR_NAME);
        }

        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            Log.e(TAG, "Failed to create log directory");
        }
    }

    private static String buildCrashReport(Throwable throwable) {
        StringBuilder report = new StringBuilder();

        // 添加设备信息
        report.append("Device Info:\n");
        report.append("Android API: ").append(Build.VERSION.SDK_INT).append("\n");
        report.append("Device: ").append(Build.MANUFACTURER).append(" ")
                .append(Build.MODEL).append("\n");
        report.append("App Version: ").append(BuildConfig.VERSION_NAME)
                .append(" (").append(BuildConfig.VERSION_CODE).append(")\n\n");

        // 添加异常堆栈
        report.append("Stack Trace:\n");
        report.append(Log.getStackTraceString(throwable));

        return report.toString();
    }


    // 原始Log方法的封装
    public static void v(String tag, String msg) {
        Log.v(tag, msg);
        logToFile(Log.VERBOSE, tag, msg, null);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        logToFile(Log.DEBUG, tag, msg, null);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
        logToFile(Log.INFO, tag, msg, null);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
        logToFile(Log.WARN, tag, msg, null);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        logToFile(Log.ERROR, tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        logToFile(Log.ERROR, tag, msg, tr);
    }

    private static void logToFile(int level, String tag, String msg, Throwable tr) {
        if (instance == null || !instance.isEnabled) return;

        String logContent = instance.formatLogContent(level, tag, msg, tr);
        instance.writeToFile(logContent);
    }

    private String formatLogContent(int level, String tag, String message, Throwable tr) {
        String levelStr = getLevelString(level);
        String timestamp = logDateFormat.format(new Date());
        StringBuilder sb = new StringBuilder();

        sb.append(timestamp)
                .append(" ").append(levelStr)
                .append("/").append(tag)
                .append(": ").append(message);

        if (tr != null) {
            sb.append("\n").append(Log.getStackTraceString(tr));
        }

        return sb.append("\n").toString();
    }

    private String getLevelString(int logLevel) {
        switch (logLevel) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            default:
                return "?";
        }
    }

    private void writeToFile(String content) {
        logExecutor.execute(() -> {
            try {
                String dateString = fileDateFormat.format(new Date());
                File logFile = new File(logDirectory, LOG_FILE_PREFIX + dateString + LOG_FILE_EXT);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                    writer.write(content);
                }

                cleanOldLogs();
            } catch (IOException e) {
                Log.e(TAG, "Error writing log to file", e);
            }
        });
    }

    private void cleanOldLogs() {
        File[] files = logDirectory.listFiles((dir, name) -> name.startsWith(LOG_FILE_PREFIX));
        if (files == null || files.length <= MAX_LOG_FILES) return;

        long oldestAllowed = System.currentTimeMillis() - (MAX_LOG_FILES * 24 * 60 * 60 * 1000L);

        for (File file : files) {
            if (file.lastModified() < oldestAllowed) {
                file.delete();
            }
        }
    }

    public static File getLogDirectory() {
        return instance != null ? instance.logDirectory : null;
    }

    public static File getTodayLogFile() {
        if (instance == null) return null;
        String dateString = instance.fileDateFormat.format(new Date());
        return new File(instance.logDirectory, LOG_FILE_PREFIX + dateString + LOG_FILE_EXT);
    }
}