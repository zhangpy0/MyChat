package top.zhangpy.mychat.ui.view;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.databinding.ActivityPersonalInfoBinding;
import top.zhangpy.mychat.ui.viewmodel.PersonalInfoViewModel;

public class PersonalInfoActivity extends AppCompatActivity {

    private ActivityPersonalInfoBinding binding; // ViewBinding
    private PersonalInfoViewModel viewModel; // ViewModel

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(PersonalInfoViewModel.class);
        viewModel.updateUserInfoFromLocalAndServer();

        // 设置监听器和数据绑定
        setupObservers();
        setupClickListeners();

        // 返回按钮事件
        binding.btnBack.setOnClickListener(v -> {
            viewModel.updateToLocalAndServer();
        });

        viewModel.getUpdateResult().observe(this, updateResult -> {
            if (updateResult) {
                finish();
            }
        });

        viewModel.updateUserInfoFromLocalAndServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.updateUserInfoFromLocalAndServer();
    }

    private void setupObservers() {
        // 头像
        viewModel.getAvatar().observe(this, avatar -> {

//            Toast.makeText(this, "头像加载: " + avatar, Toast.LENGTH_SHORT).show();
            if (avatar == null || avatar.isEmpty()) {
                Glide.with(this)
                        .load(R.drawable.default_avatar)
                        .into(binding.ivAvatar);
                return;
            }

            // glide 需要禁用内存缓存，否则会加载虚空图片

            Glide.with(this)
                    .load(avatar)
                    .skipMemoryCache(true) // 跳过内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                    .placeholder(R.drawable.default_avatar) // 占位符
                    .error(R.drawable.default_avatar) // 加载失败时的图片
                    .into(binding.ivAvatar);
        });

        // 昵称
        viewModel.getNickname().observe(this, nickname -> {
            binding.tvNickname.setText(nickname);
        });

        // 账号
        viewModel.getAccount().observe(this, account -> {
            binding.tvAccount.setText(account);
        });

        // 性别
        viewModel.getGender().observe(this, gender -> {
            binding.tvGender.setText(gender);
        });

        // 地区
        viewModel.getRegion().observe(this, region -> {
            binding.tvRegion.setText(region);
        });
    }

    private void setupClickListeners() {

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String newPath = result.getData().getStringExtra("result_avatar");
                String newGender = result.getData().getStringExtra("result_gender");
                int key = result.getData().getIntExtra("result_key", 0);
                String newText = result.getData().getStringExtra("result_text");
                if (newPath != null) {
                    viewModel.updateAvatar(newPath);
                }
                if (newGender != null) {
                    viewModel.updateGender(newGender);
                }
                if (key != 0 && newText != null) {
                    if (key == 1) {
                        viewModel.updateNickname(newText);
                    } else if (key == 2) {
                        viewModel.updateRegion(newText);
                    }
                }
//                viewModel.updateUserInfoFromLocalAndServer();
            }
        });
        // 点击头像
        binding.llAvatar.setOnClickListener(v -> {
//            Toast.makeText(this, "修改头像", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditAvatarActivity.class);
            launcher.launch(intent);
        });

        // 点击昵称
        binding.llNickname.setOnClickListener(v -> {
//            Toast.makeText(this, "修改昵称", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditTextActivity.class);
            intent.putExtra(EditTextActivity.EXTRA_TITLE, "更改昵称");
            launcher.launch(intent);
        });

        // 点击账号
        binding.llAccount.setOnClickListener(v -> {
            Toast.makeText(this, "账号不可修改", Toast.LENGTH_SHORT).show();
            // 跳转到查看账号界面（账号一般不可修改）
            // 示例：startActivity(new Intent(this, AccountDetailActivity.class));
        });

        // 点击性别
        binding.llGender.setOnClickListener(v -> {
//            Toast.makeText(this, "修改性别", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditGenderActivity.class);
            launcher.launch(intent);
        });

        // 点击地区
        binding.llRegion.setOnClickListener(v -> {
//            Toast.makeText(this, "修改地区", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditTextActivity.class);
            intent.putExtra(EditTextActivity.EXTRA_TITLE, "更改地区");
            launcher.launch(intent);
        });
    }
}
