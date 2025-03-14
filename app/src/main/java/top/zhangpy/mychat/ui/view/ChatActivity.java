package top.zhangpy.mychat.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.data.service.DownloadService;
import top.zhangpy.mychat.ui.adapter.MessageAdapter;
import top.zhangpy.mychat.ui.viewmodel.ChatViewModel;
import top.zhangpy.mychat.util.HideKeyboard;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private ChatViewModel viewModel;

    private ActivityResultLauncher<Intent> imagePreviewLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    private EditText inputMessage;

    private ImageView moreButton;
    private Button sendButton;

    private Button selectPhotoButton;

    private Button selectFileButton;

    private LinearLayout moreLayout;

    private TextView contactName;

    private ImageView btnBack;

    private ImageView contactInfo;

    private int contactId;

    private final BroadcastReceiver messageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.UPDATE_MESSAGES".equals(intent.getAction())) {
                int updatedContactId = intent.getIntExtra("contact_id", -1);
                if (updatedContactId == contactId) { // 仅处理当前联系人的消息
                    viewModel.updateMessagesFromLocal(contactId);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        LinearLayout rootLayout = findViewById(R.id.root_layout);

        Logger.initialize(getApplicationContext());
        Logger.enableLogging(true);

        // 设置触摸监听器
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                HideKeyboard.hideKeyboardAndClearFocus(this); // 调用工具方法
            }
            return false; // 继续分发触摸事件
        });

        contactId = getIntent().getIntExtra("contact_id", -1);
        Logger.d("ChatActivity", "contactId: " + contactId);

        DownloadService.startService(this, contactId);

        // 注册 ActivityResultLauncher
        imagePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Logger.d("ChatActivity", "selectedImageUri: " + selectedImageUri);
                        if (selectedImageUri != null) {
                            String path = StorageHelper.getRealPathFromURI(this, selectedImageUri.toString());
                            if (path == null) {
                                path = selectedImageUri.getPath();
                            }
                            Logger.d("ChatActivity", "path: " + path);
                            viewModel.sendMessageToFriend(contactId, "", "image", path);
                        } else {
                            Toast.makeText(this, "无法获取图片路径", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 注册文件选择器的 ActivityResultLauncher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectFileUri = result.getData().getData();
                        Logger.d("ChatActivity", "selectFileUri: " + selectFileUri);
                        if (selectFileUri != null) {
                            // 获取文件路径
                            String filePath = StorageHelper.getRealPathFromURI(this, String.valueOf(selectFileUri));
                            if (filePath != null) {
                                viewModel.sendMessageToFriend(contactId, "", "file", filePath);
                            } else {
                                Toast.makeText(this, "无法获取文件路径", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "未选择文件", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 初始化视图
        recyclerView = findViewById(R.id.rv_messages);
        inputMessage = findViewById(R.id.et_message_input);
        moreButton = findViewById(R.id.btn_more);
        sendButton = findViewById(R.id.btn_send);
        moreLayout = findViewById(R.id.layout_more_options);
        selectPhotoButton = findViewById(R.id.btn_select_photo);
        selectFileButton = findViewById(R.id.btn_select_file);
        contactName = findViewById(R.id.tv_contact_name);
        btnBack = findViewById(R.id.btn_back);
        contactInfo = findViewById(R.id.btn_contact_details);

        // 输入框设置
        inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // 防止换行时退出输入状态
                inputMessage.append("\n");
                return true; // 返回 true 表示消费事件
            }
            return false;
        });

        // ViewModel初始化
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
//        String myAvatarPath = viewModel.getMyAvatar();
//        String otherAvatarPath = viewModel.getFriendAvatar(contactId);



        viewModel.getFriendName().observe(this, friendName -> {
            contactName.setText(friendName);
        });

        // 初始化RecyclerView
        adapter = new MessageAdapter(
                this,
                new ArrayList<>(), // 初始数据为空
                null,
                null,
                contactId

        );
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewModel.updateMessagesFromLocal(contactId); // 加载历史消息

        setBind(contactId);

        viewModel.getMyAvatarPath().observe(this, myAvatar -> checkAndResetAdapter(contactId));

        viewModel.getFriendAvatarPath().observe(this, friendAvatar -> checkAndResetAdapter(contactId));

        // 加载头像
        viewModel.loadMyAvatar();
        viewModel.loadFriendAvatar(contactId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("top.zhangpy.mychat.UPDATE_MESSAGES");
        registerReceiver(messageUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }


    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setAllMessagesRead(contactId);
        unregisterReceiver(messageUpdateReceiver);
    }


    private void setBind(Integer contactId) {

//        // 监听ViewModel中的数据变化
//        viewModel.getMessages().observe(this, messages -> {
//            adapter.getMessages().clear();
//            adapter.getMessages().addAll(messages);
//            // TODO 优化
//            adapter.notifyDataSetChanged();
//            recyclerView.scrollToPosition(messages.size() - 1);
//        });
        viewModel.getMessages().observe(this, messages -> {
            adapter.getMessages().clear();
            adapter.getMessages().addAll(messages);

            // 优化更新方式
            adapter.notifyItemRangeChanged(0, messages.size());

            // 延迟滚动确保布局完成
            recyclerView.post(() -> {
                if (messages.size() > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    layoutManager.scrollToPositionWithOffset(messages.size() - 1, 0);
                    layoutManager.setStackFromEnd(true);
                }
            });
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
            Intent intent = new Intent(ChatActivity.this, ImagePreviewActivity.class);
            intent.putExtra("contact_id", contactId); // 传递 contactId
            imagePreviewLauncher.launch(intent);
        });

        selectFileButton.setOnClickListener(v -> {
            // TODO
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // 设置为所有类型的文件
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "选择要发送文件"));
        });

        contactInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ContactInfoActivity.class);
            intent.putExtra("contact_id", contactId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void resetAdapter(String myAvatar, String friendAvatar) {
        adapter.setMyAvatarPath(myAvatar);
        adapter.setOtherAvatarPath(friendAvatar);
//        recyclerView.setAdapter(adapter);
        // TODO 优化
        adapter.notifyDataSetChanged();
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void checkAndResetAdapter(int contactId) {
        Logger.d("ChatActivity", "checkAndResetAdapter");
        String myAvatar = viewModel.getMyAvatarPath().getValue();
        String friendAvatar = viewModel.getFriendAvatarPath().getValue();
        if (myAvatar != null && friendAvatar != null) { // 确保只初始化一次
            resetAdapter(myAvatar, friendAvatar);
            viewModel.updateMessagesFromLocal(contactId); // 加载历史消息
        }
    }
}
