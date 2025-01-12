package top.zhangpy.mychat.ui.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.canhub.cropper.CropImageView;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.ChatViewModel;

public class ImagePreviewActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ChatViewModel viewModel;
    private CropImageView cropImageView;

    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        contactId = getIntent().getIntExtra("contact_id", -1);

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(ChatViewModel.class);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        }

        // 初始化 CropImageView
        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setScaleType(CropImageView.ScaleType.CENTER_INSIDE); // 确保图片以合适比例显示
        cropImageView.setGuidelines(CropImageView.Guidelines.ON); // 显示裁剪引导线

        // 打开相册选择图片
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // 显示图片到编辑区域
                    loadImageToPreview(selectedImageUri);
                }
            } else {
                finish(); // 如果用户未选择图片，则退出编辑界面
            }
        });

        // 启动图片选择器
        launcher.launch(intent);

        // 按钮逻辑
        Button sendButton = findViewById(R.id.btn_send);
        Button cancelButton = findViewById(R.id.btn_cancel);

        sendButton.setOnClickListener(v -> {
            Uri croppedImageUri = cropImageView.getImageUri();
            if (croppedImageUri != null) {
                viewModel.sendMessageToFriend(contactId, "", "image", String.valueOf(croppedImageUri));
                Intent intent1 = new Intent();
                setResult(RESULT_OK, intent1);
                finish(); // 发送完成后退出当前界面
            }
        });

        cancelButton.setOnClickListener(v -> finish()); // 点击取消按钮退出界面
    }

    private void loadImageToPreview(Uri imageUri) {
        // 加载图片到 CropImageView
        cropImageView.setImageUriAsync(imageUri);
    }
}
