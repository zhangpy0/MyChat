package top.zhangpy.mychat.ui.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.SettingsViewModel;
import top.zhangpy.mychat.util.Logger;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Logger.initialize(getApplicationContext());
        Logger.enableLogging(true);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 退出登录逻辑
        LinearLayout llLogout = findViewById(R.id.ll_logout);
        llLogout.setOnClickListener(v -> {
            // 调用 ViewModel 的注销逻辑
            viewModel.logout();

            // 跳转到 Welcome 页面并清除当前栈
            Intent intent = new Intent(this, AppStartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        LinearLayout llCheckLog = findViewById(R.id.ll_check_log);
        llCheckLog.setOnClickListener(v -> openLogDirectory(this));
    }


    public void openLogDirectory(Context context) {
//        File logDir = Logger.getLogDirectory();
        File logDir = Logger.getTodayLogFile();
        Logger.i("SettingsActivity", "openLogDirectory: " + logDir);
        if (logDir == null || !logDir.exists()) {
            Toast.makeText(context, "日志文件夹未找到", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                logDir
        );

        Logger.i("SettingsActivity", "openLogDirectory uri : " + uri);

//        intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR);
        intent.setDataAndType(uri, "text/plain");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }

        // 授予临时权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        // 验证是否有可用应用
        PackageManager pm = context.getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            context.startActivity(Intent.createChooser(intent, "Open log directory"));
        } else {
            // 备用方案：显示目录路径
            String path = logDir.getAbsolutePath();
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Log Path", path);
            clipboard.setPrimaryClip(clip);

            new AlertDialog.Builder(context)
                    .setTitle("Log Directory")
                    .setMessage("日志路径已粘贴到剪贴板:\n" + path)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
