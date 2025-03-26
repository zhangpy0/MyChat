package top.zhangpy.mychat.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    // 权限请求码
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final int MANAGE_STORAGE_PERMISSION_REQUEST_CODE = 101;

    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;

    // 检查是否有存储权限
    public static boolean hasStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14及以上
            return isManageStoragePermissionGranted();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11.0至Android 13
            return (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    || Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0至Android 11
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 6.0以下不需要动态权限
            return true;
        }
    }

    // 检查是否已授予 MANAGE_EXTERNAL_STORAGE 权限（管理所有文件权限）
    public static boolean isManageStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    Environment.isExternalStorageManager();
        }
        return false;
    }

    // TODO 优化
    // 请求存储权限（包括管理所有文件权限）
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Android 14 或更高版本，要求 MANAGE_EXTERNAL_STORAGE 权限
            if (!isManageStoragePermissionGranted()) {
                requestManageStoragePermission(activity);
            } else {
                // 如果已经有权限，则允许访问照片和视频
//                openFilePicker(activity);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 至 Android 13 请求传统的存储权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
//                openFilePicker(activity);
            }
        }
    }

    // 请求管理所有文件的权限
    private static void requestManageStoragePermission(Activity activity) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_REQUEST_CODE);
    }

    // 处理用户请求权限的结果
    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;  // 权限未被授予
                }
            }
            return true;  // 所有权限都被授予
        } else if (requestCode == MANAGE_STORAGE_PERMISSION_REQUEST_CODE) {
            return isManageStoragePermissionGranted();  // 检查管理存储权限是否已授予
        }
        return false;
    }

    // 打开文件选择器（图片、视频等）
    public static void openFilePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");  // 通用类型，可以修改为具体类型，如 "image/*" 或 "video/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
    }

    // TODO 优化
    // 检查是否需要请求管理所有文件的权限（适用于 Android 14+）
    public static void checkForManageStoragePermission(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !isManageStoragePermissionGranted()) {
        if (true) {
            Toast.makeText(activity, "请授予应用管理所有文件的权限(打开应用设置)", Toast.LENGTH_LONG).show();
            requestStoragePermission(activity);
        } else {
            // 如果权限已经授予，可以直接打开文件选择器或继续操作
//            openFilePicker(activity);
        }
    }

    // 检查通知权限是否已授予
    public static boolean isNotificationPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager != null && notificationManager.areNotificationsEnabled();
        }
        return true;  // Android 13 以下版本默认授予通知权限
    }

    // 请求通知权限（适用于 Android 13 及以上版本）
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationPermissionGranted(activity)) {
                Toast.makeText(activity, "请授予应用通知权限", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
                activity.startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // 电池白名单
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        String packageName = context.getPackageName();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isIgnoringBatteryOptimizations(packageName);
        }
        return false;
    }

    public static void requestIgnoreBatteryOptimizations(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("请将应用加入电池白名单，以确保消息能够及时推送。");
        builder.setPositiveButton("确定", (dialog, which) -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
    }
}
