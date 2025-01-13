package top.zhangpy.mychat.ui.view;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import top.zhangpy.mychat.R;

public class ImageViewActivity extends AppCompatActivity {

    private PhotoView photoView;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageUrl = getIntent().getStringExtra("image_url");
        Log.d("ImageViewActivity", "onCreate: " + imageUrl);

        photoView = findViewById(R.id.ImageView);


        loadImageToView(imageUrl);

        photoView.setOnClickListener(v -> finish());
    }

    private void loadImageToView(String imageUri) {
        Glide.with(this)
                .load(imageUri)
                .skipMemoryCache(true) // 跳过内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .placeholder(R.drawable.default_avatar) // 占位符
                .error(R.drawable.default_avatar) // 加载失败时的图片
                .into(photoView);
    }
}
