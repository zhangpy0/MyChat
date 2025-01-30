package top.zhangpy.mychat.ui.view;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.TabModel;
import top.zhangpy.mychat.ui.view.fragments.ContactListFragment;
import top.zhangpy.mychat.ui.view.fragments.FindFragment;
import top.zhangpy.mychat.ui.view.fragments.SelfFragment;
import top.zhangpy.mychat.ui.view.fragments.WeixinFragment;
import top.zhangpy.mychat.ui.viewmodel.ChatListViewModel;
import top.zhangpy.mychat.util.DoubleSwipeBackHelper;
import top.zhangpy.mychat.util.PermissionUtils;

public class ChatListActivity extends FragmentActivity {

    private ChatListViewModel viewModel;

    private Fragment weixinFragment;
    private Fragment contactListFragment;
    private Fragment findFragment;
    private Fragment selfFragment;

    private DoubleSwipeBackHelper swipeBackHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_chatlist);
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        swipeBackHelper = new DoubleSwipeBackHelper(this);

        findViewById(R.id.weixin_layout).setOnClickListener(v -> viewModel.setSelectedTab(0));
        findViewById(R.id.contacts_layout).setOnClickListener(v -> viewModel.setSelectedTab(1));
        findViewById(R.id.find_layout).setOnClickListener(v -> viewModel.setSelectedTab(2));
        findViewById(R.id.self_layout).setOnClickListener(v -> viewModel.setSelectedTab(3));

        setupObservers();

        if (savedInstanceState == null) {
            viewModel.setSelectedTab(0);
        }

        if (!PermissionUtils.hasStoragePermission(this)) {
            PermissionUtils.checkForManageStoragePermission(this);
        }
        if (!PermissionUtils.isNotificationPermissionGranted(this)) {
            PermissionUtils.requestNotificationPermission(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeBackHelper.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setupObservers() {
        viewModel.getSelectedTab().observe(this, this::updateTabSelection);
    }

    private void updateTabSelection(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideAllFragments(transaction);

        Fragment fragmentToShow = null;
        switch (index) {
            case 0:
                if (weixinFragment == null) {
                    weixinFragment = new WeixinFragment();
                    transaction.add(R.id.fragment, weixinFragment, "WEIXIN_FRAGMENT");
                }
                fragmentToShow = weixinFragment;
                break;
            case 1:
                if (contactListFragment == null) {
                    contactListFragment = new ContactListFragment();
                    transaction.add(R.id.fragment, contactListFragment, "CONTACT_LIST_FRAGMENT");
                }
                fragmentToShow = contactListFragment;
                break;
            case 2:
                if (findFragment == null) {
                    findFragment = new FindFragment();
                    transaction.add(R.id.fragment, findFragment, "FIND_FRAGMENT");
                }
                fragmentToShow = findFragment;
                break;
            case 3:
                if (selfFragment == null) {
                    selfFragment = new SelfFragment();
                    transaction.add(R.id.fragment, selfFragment, "SELF_FRAGMENT");
                }
                fragmentToShow = selfFragment;
                break;
        }

        if (fragmentToShow != null) {
            transaction.show(fragmentToShow);
        }

        transaction.commit();

        // 更新底部标签UI（图标和文字颜色）
        updateTabUI(index);
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (weixinFragment != null) transaction.hide(weixinFragment);
        if (contactListFragment != null) transaction.hide(contactListFragment);
        if (findFragment != null) transaction.hide(findFragment);
        if (selfFragment != null) transaction.hide(selfFragment);
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
