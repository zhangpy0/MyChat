package top.zhangpy.mychat.ui.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.canhub.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.PersonalInfoViewModel;
import top.zhangpy.mychat.util.Logger;

public class EditAvatarActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private PersonalInfoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_avatar);

        Logger.initialize(getApplicationContext());
        Logger.enableLogging(true);

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(PersonalInfoViewModel.class);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        }
        // 打开相册选择图片
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    // 显示图片到编辑区域
                    loadImageToEditor(selectedImageUri);
                }
            } else {
                finish(); // 如果用户未选择图片，则退出编辑界面
            }
        });

        CropImageView cropImageView = findViewById(R.id.cropImageView);
        Button btnCancel = findViewById(R.id.btn_cancel);
        ImageButton btnUndo = findViewById(R.id.btn_undo);
        ImageButton btnRotate = findViewById(R.id.btn_rotate);
        Button btnSave = findViewById(R.id.btn_save);

        // 初始化裁剪控件
        cropImageView.setFixedAspectRatio(true); // 固定为正方形裁剪
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);

        // 按钮事件
        btnCancel.setOnClickListener(v -> finish());

        btnUndo.setOnClickListener(v -> cropImageView.resetCropRect()); // 撤销裁剪框

        btnRotate.setOnClickListener(v -> cropImageView.rotateImage(90)); // 顺时针旋转 90 度

        btnSave.setOnClickListener(v -> {
            // 保存裁剪后的图片
            Bitmap croppedImage = cropImageView.getCroppedImage();
            if (croppedImage != null) {
                saveCroppedImage(croppedImage);
            }
        });

        viewModel.getUpdateResult().observe(this, success -> {
            if (success) {
                finish();
            }
        });

        launcher.launch(intent);
    }

    private void loadImageToEditor(Uri imageUri) {
        // 使用 ImageView 或自定义视图加载图片
        CropImageView cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageUriAsync(imageUri);
    }

    // 保存裁剪后的图片
    private void saveCroppedImage(Bitmap croppedImage) {
        try {
            // 保存到应用缓存目录
            File file = new File(getCacheDir(), "cropped_avatar.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();

            // 返回裁剪后的图片路径
            Intent resultIntent = new Intent();
            resultIntent.putExtra("croppedImageUri", file.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);

            Logger.d("EditAvatarActivity", "保存cache成功: " + file.getAbsolutePath());

            String newPath = viewModel.updateUserAvatar(file.getAbsolutePath());
            resultIntent.putExtra("result_avatar", newPath);
            setResult(RESULT_OK, resultIntent);


        } catch (IOException e) {
            Logger.e("EditAvatarActivity", "保存失败", e);
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
