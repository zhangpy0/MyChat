package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.FileViewModel;
import top.zhangpy.mychat.util.StorageHelper;

public class FileViewActivity extends AppCompatActivity {

    private FileViewModel fileViewModel;

    private int messageId;

    private String filePath;

    private String fileName;

    private Long fileSize;

    private Boolean isMe;

    private String content;

    private int contactId;

    private String contactType;

    private ImageButton btnBack;
    private TextView tvTitle;
    private ImageView ivFileIcon;
    private TextView tvFileName;
    private TextView tvFileSize;
    private Button btnDownload;
    private Button btnOpen;
    private LinearLayout llDownload;
    private ProgressBar pbDownload;
    private TextView tvDownloadInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);

        fileViewModel = new ViewModelProvider(this).get(FileViewModel.class);

        contactId = getIntent().getIntExtra("contact_id", -1);
        messageId = getIntent().getIntExtra("id", -1);
        filePath = getIntent().getStringExtra("file_path");
        fileName = getIntent().getStringExtra("file_name");
        fileSize = getIntent().getLongExtra("file_size", 0);
        isMe = getIntent().getBooleanExtra("is_me", false);
        content = getIntent().getStringExtra("content");
        contactType = getIntent().getStringExtra("contact_type");

        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        ivFileIcon = findViewById(R.id.iv_file_type_icon);
        tvFileName = findViewById(R.id.tv_file_name);
        tvFileSize = findViewById(R.id.tv_file_size);
        btnDownload = findViewById(R.id.btn_download);
        btnOpen = findViewById(R.id.btn_open);
        llDownload = findViewById(R.id.ll_download_progress);
        pbDownload = findViewById(R.id.pb_download_progress);
        tvDownloadInfo = findViewById(R.id.tv_download_info);

        tvTitle.setText(fileName);
        tvFileName.setText(fileName);
        tvFileSize.setText(StorageHelper.formatFileSize(fileSize));

        // TODO 设置文件类型图标
        // ivFileIcon

        boolean isDownloaded = fileViewModel.isFileDownloaded(contactId, messageId, filePath);
        llDownload.setVisibility(View.GONE);
        if (isDownloaded) {
            btnDownload.setVisibility(View.GONE);
            btnOpen.setVisibility(View.VISIBLE);
        } else {
            btnDownload.setVisibility(View.VISIBLE);
            btnOpen.setVisibility(View.GONE);
            btnOpen.setActivated(false);
        }
        setBind();
    }

    private void setBind() {
        btnBack.setOnClickListener(v -> finish());

        fileViewModel.getIsDownloading().observe(this, isDownloading -> {
            if (isDownloading) {
                pbDownload.setVisibility(View.VISIBLE);
                tvDownloadInfo.setText("下载中...");
            }
        });

        fileViewModel.getIsFileDownloaded().observe(this, isDownloaded -> {
            if (isDownloaded) {
                llDownload.setVisibility(View.GONE);
                Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();
                btnOpen.setVisibility(View.VISIBLE);
                btnOpen.setActivated(true);
                btnDownload.setVisibility(View.GONE);
                filePath = fileViewModel.getFilePath();
            }
        });

        btnDownload.setOnClickListener(v -> {
            fileViewModel.downloadFile(contactId, messageId, contactType);
            llDownload.setVisibility(View.VISIBLE);
            btnDownload.setVisibility(View.GONE);
            btnOpen.setActivated(false);
        });

        btnOpen.setOnClickListener(v -> {
            if ((filePath == null || filePath.isEmpty()) && (filePath = fileViewModel.getFilePath() == null ? "" : fileViewModel.getFilePath()).isEmpty()) {
                Toast.makeText(this, "文件无效", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            // 根据文件路径创建 Uri
            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0 及以上使用 FileProvider
                fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            } else {
                fileUri = Uri.fromFile(file);
            }

            // 获取文件的 MIME 类型
            String mimeType = getMimeType(filePath);

            // 创建 Intent
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, mimeType);
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 授予临时权限

            // 检查是否有应用可以处理这个 Intent
            if (openIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openIntent);
            } else {
                Toast.makeText(this, "没有应用可以打开此文件", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMimeType(String filePath) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return "*/*"; // 默认返回通用类型
    }

}
