package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.ContactInfoViewModel;

public class ContactInfoActivity extends AppCompatActivity {
    private ContactInfoViewModel contactInfoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        int contactId = getIntent().getIntExtra("contact_id", -1);

        // 获取 ViewModel
        contactInfoViewModel = new ViewModelProvider(this).get(ContactInfoViewModel.class);

        // 获取 Intent 数据
        contactInfoViewModel.updateUserInfoFromLocalAndServer(contactId);

        // 绑定数据到 UI
        TextView nickname = findViewById(R.id.tv_nickname);
        TextView account = findViewById(R.id.tv_account);
        TextView gender = findViewById(R.id.tv_gender);
        TextView region = findViewById(R.id.tv_region);
        ImageView avatar = findViewById(R.id.iv_avatar);

        contactInfoViewModel.getUpdateResult().observe(this, updateResult -> {
            if (updateResult) {
                nickname.setText(contactInfoViewModel.getNickname().getValue());
                account.setText(contactInfoViewModel.getAccount().getValue());
                region.setText(contactInfoViewModel.getRegion().getValue());
                gender.setText(contactInfoViewModel.getGender().getValue());
                Glide.with(this)
                        .load(contactInfoViewModel.getAvatarPath().getValue())
                        .skipMemoryCache(true) // 跳过内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                        .placeholder(R.drawable.default_avatar) // 占位符
                        .error(R.drawable.default_avatar) // 加载失败时的图片
                        .into(avatar);
            }
        });

        // 设置返回按钮点击事件
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());

        // 设置“发消息”按钮点击事件
        findViewById(R.id.ll_settings).setOnClickListener(view -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("contact_id", contactId);
            startActivity(intent);
            finish();
        });
    }
}
