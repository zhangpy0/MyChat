package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        boolean isSearch = getIntent().getBooleanExtra("is_search", false);

        if (contactId == -1) {
            Toast.makeText(this, "联系人不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        LinearLayout interactionLayout = findViewById(R.id.layout_interact);
        TextView tvInteract = findViewById(R.id.tv_interact);


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

        contactInfoViewModel.getUpdateResultIsFriend().observe(this, updateResultIsFriend -> {
            if (updateResultIsFriend) {
                boolean isFriend = contactInfoViewModel.getIsFriend().getValue();
                if (isFriend) {
                    interactionLayout.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("contact_id", contactId);
                        startActivity(intent);
                    });
                } else {
                    tvInteract.setText("添加好友");
                    findViewById(R.id.iv_settings_icon).setVisibility(View.GONE);
                    interactionLayout.setOnClickListener(view -> {
                        Intent intent = new Intent(this, SendApplyActivity.class);
                        intent.putExtra("contact_id", contactId);
                        startActivity(intent);
                    });
                }
            }
        });

        contactInfoViewModel.isMyFriend(contactId);

        // 设置返回按钮点击事件
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
    }
}
