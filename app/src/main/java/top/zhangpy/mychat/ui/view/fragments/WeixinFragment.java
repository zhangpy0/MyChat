package top.zhangpy.mychat.ui.view.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.adapter.ChatListAdapter;
import top.zhangpy.mychat.ui.viewmodel.WeixinViewModel;

@SuppressLint("ValidFragment")
public class WeixinFragment extends Fragment {
    private WeixinViewModel viewModel;
    private RecyclerView messageList;
    private ChatListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载布局
        return inflater.inflate(R.layout.weixin_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
//        TextView title = view.findViewById(R.id.weixin_title);
        messageList = view.findViewById(R.id.listView);

        // 配置RecyclerView
        messageList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter();
        messageList.setAdapter(adapter);

        // 获取ViewModel并观察数据
        viewModel = new ViewModelProvider(this).get(WeixinViewModel.class);
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            // 更新RecyclerView的数据
            adapter.setMessages(messages);
        });

        viewModel.updateMessages();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.updateMessages();
        IntentFilter filter = new IntentFilter("top.zhangpy.mychat.UPDATE_MESSAGES");
        requireContext().registerReceiver(messageUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(messageUpdateReceiver);
    }

    private final BroadcastReceiver messageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("top.zhangpy.mychat.UPDATE_MESSAGES".equals(intent.getAction())) {
                viewModel.updateMessages();
                Intent intentClear = new Intent("top.zhangpy.mychat.CLEAR_NOTIFICATIONS");
                requireContext().sendBroadcast(intentClear);
            }
        }
    };
}


