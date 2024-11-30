package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.adapter.ApplyListAdapter;
import top.zhangpy.mychat.ui.viewmodel.AddFriendViewModel;

public class AddFriendActivity extends AppCompatActivity {

    private AddFriendViewModel viewModel;
    private ApplyListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // 初始化 ViewModel
        viewModel = new AddFriendViewModel(getApplication());

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 设置“添加朋友”按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchFriendActivity.class); // 需要实现 SearchFriendActivity
            startActivity(intent);
        });

        // 初始化 RecyclerView
        RecyclerView recyclerView = findViewById(R.id.friend_request_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplyListAdapter(viewModel);
        recyclerView.setAdapter(adapter);

        // 观察好友申请列表
        viewModel.getApplyList().observe(this, applyList -> {
            adapter.setApplyListItems(applyList);
        });

        // 搜索框点击事件
        findViewById(R.id.search_layout).setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchFriendActivity.class);
            startActivity(intent);
        });

        viewModel.getUpdateStatus().observe(this, applyId -> {
            if (applyId != null) {
                adapter.updateItemStatus(applyId); // 刷新特定项
            }
        });

        // 请求好友申请数据
        viewModel.updateApplyListFromServer();
    }
}
