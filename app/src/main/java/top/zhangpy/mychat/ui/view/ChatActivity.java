package top.zhangpy.mychat.ui.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.adapter.MessageAdapter;
import top.zhangpy.mychat.ui.viewmodel.ChatViewModel;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private ChatViewModel viewModel;

    private EditText inputMessage;

    private ImageView moreButton;
    private Button sendButton;

    private Button selectPhotoButton;

    private LinearLayout moreLayout;

    private TextView contactName;

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int contactId = getIntent().getIntExtra("contact_id", -1);

        // 初始化视图
        recyclerView = findViewById(R.id.rv_messages);
        inputMessage = findViewById(R.id.et_message_input);
        moreButton = findViewById(R.id.btn_more);
        sendButton = findViewById(R.id.btn_send);
        moreLayout = findViewById(R.id.layout_more_options);
        selectPhotoButton = findViewById(R.id.btn_select_photo);
        contactName = findViewById(R.id.tv_contact_name);
        btnBack = findViewById(R.id.btn_back);

        // ViewModel初始化
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        String myAvatarPath = viewModel.getMyAvatar();
        String otherAvatarPath = viewModel.getFriendAvatar(contactId);

        viewModel.getIsAvatarUpdated().observe(this, updated -> {
            if (updated >= 2) {
                adapter = new MessageAdapter(
                        this,
                        new ArrayList<>(), // 初始数据为空
                        myAvatarPath,
                        otherAvatarPath
                );
            }
        });

        viewModel.getFriendName().observe(this, friendName -> {
            contactName.setText(friendName);
        });

        // 初始化RecyclerView
        adapter = new MessageAdapter(
                this,
                new ArrayList<>(), // 初始数据为空
                "",
                ""
        );
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 监听ViewModel中的数据变化
        viewModel.getMessages().observe(this, messages -> {
            adapter.getMessages().clear();
            adapter.getMessages().addAll(messages);
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);
        });

        // 发送按钮事件
        sendButton.setOnClickListener(v -> {
            String content = inputMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.sendMessageToFriend(contactId, content, "text", null);
                inputMessage.setText("");
                viewModel.updateMessagesFromLocal(contactId);
            } else {
                Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        moreButton.setOnClickListener(v -> {
            if (moreLayout.getVisibility() == View.VISIBLE) {
                moreLayout.setVisibility(View.GONE);
            } else {
                moreLayout.setVisibility(View.VISIBLE);
            }
        });

        selectPhotoButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 申请权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            // 打开相册选择图片
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // 显示图片到编辑区域
                        Intent intent2 = new Intent(this, ImagePreviewActivity.class);
                        intent2.putExtra("image_uri", selectedImageUri.toString());
                        startActivity(intent2);
                    }
                } else {
                    finish(); // 如果用户未选择图片，则退出编辑界面
                }
            });
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 加载历史消息
        viewModel.updateMessagesFromLocal(contactId);
    }


}
