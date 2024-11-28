package top.zhangpy.mychat.ui.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.TabModel;
import top.zhangpy.mychat.ui.view.fragments.ContactListFragment;
import top.zhangpy.mychat.ui.view.fragments.FindFragment;
import top.zhangpy.mychat.ui.view.fragments.SelfFragment;
import top.zhangpy.mychat.ui.view.fragments.WeixinFragment;
import top.zhangpy.mychat.ui.viewmodel.ChatListViewModel;

public class ChatListActivity extends FragmentActivity {

    private ChatListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_chatlist);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        findViewById(R.id.weixin_layout).setOnClickListener(v -> viewModel.setSelectedTab(0));
        findViewById(R.id.contacts_layout).setOnClickListener(v -> viewModel.setSelectedTab(1));
        findViewById(R.id.find_layout).setOnClickListener(v -> viewModel.setSelectedTab(2));
        findViewById(R.id.self_layout).setOnClickListener(v -> viewModel.setSelectedTab(3));

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getSelectedTab().observe(this, this::updateTabSelection);
    }

    private void updateTabSelection(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new WeixinFragment();
                break;
            case 1:
                fragment = new ContactListFragment();
                break;
            case 2:
                fragment = new FindFragment();
                break;
            case 3:
                fragment = new SelfFragment();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }

        // 更新底部标签UI（图标和文字颜色）
        updateTabUI(index);
    }

    private void updateTabUI(int selectedIndex) {
        for (int i = 0; i < 4; i++) {
            TabModel data = viewModel.getDefalutTabData(i);
            if (i == selectedIndex) {
                data = viewModel.getTabData(i); // 选中时的数据
            }
            updateSingleTab(i, data);
        }
    }

    private void updateSingleTab(int index, TabModel data) {
        switch (index) {
            case 0:
                ImageView weixinImg = findViewById(R.id.weixin_img);
                weixinImg.setImageResource(data.getImageResource());
                TextView weixinText = findViewById(R.id.weixin_text);
                weixinText.setTextColor(data.getTextColor());
                break;
            case 1:
                ImageView contactImg = findViewById(R.id.contact_img);
                contactImg.setImageResource(data.getImageResource());
                TextView contactText = findViewById(R.id.contact_text);
                contactText.setTextColor(data.getTextColor());
                break;
            case 2:
                ImageView findImg = findViewById(R.id.find_img);
                findImg.setImageResource(data.getImageResource());
                TextView findText = findViewById(R.id.find_text);
                findText.setTextColor(data.getTextColor());
                break;
            case 3:
                ImageView selfImg = findViewById(R.id.self_img);
                selfImg.setImageResource(data.getImageResource());
                TextView selfText = findViewById(R.id.self_text);
                selfText.setTextColor(data.getTextColor());
                break;
        }
    }
}
