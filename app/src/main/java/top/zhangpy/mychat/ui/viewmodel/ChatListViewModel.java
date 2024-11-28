package top.zhangpy.mychat.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import lombok.Getter;
import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.model.TabModel;

// 主界面 ChatList 的 ViewModel (与 ChatListItem 无关)
@Getter
public class ChatListViewModel extends ViewModel {

    private final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(0);

    public void setSelectedTab(int tabIndex) {
        selectedTab.setValue(tabIndex);
    }

    public TabModel getDefalutTabData(int tabIndex) {
        return switch (tabIndex) {
            case 0 -> new TabModel(R.drawable.tab_weixin_normal, 0xFF82858B);
            case 1 -> new TabModel(R.drawable.tab_address_normal, 0xFF82858B);
            case 2 -> new TabModel(R.drawable.tab_find_frd_normal, 0xFF82858B);
            case 3 -> new TabModel(R.drawable.tab_settings_normal, 0xFF82858B);
            default -> new TabModel(R.drawable.tab_weixin_normal, 0xFF82858B);
        };
    }

    public TabModel getTabData(int tabIndex) {
        return switch (tabIndex) {
            case 0 -> new TabModel(R.drawable.tab_weixin_pressed, 0xFF0090FF);
            case 1 -> new TabModel(R.drawable.tab_address_pressed, 0xFF0090FF);
            case 2 -> new TabModel(R.drawable.tab_find_frd_pressed, 0xFF0090FF);
            case 3 -> new TabModel(R.drawable.tab_settings_pressed, 0xFF0090FF);
            default -> new TabModel(R.drawable.tab_weixin_normal, 0xFF82858B);
        };
    }
}
