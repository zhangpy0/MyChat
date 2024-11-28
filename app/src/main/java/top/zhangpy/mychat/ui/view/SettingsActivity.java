package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
    }
}
