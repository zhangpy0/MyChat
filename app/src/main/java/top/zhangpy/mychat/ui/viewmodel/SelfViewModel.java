package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.repository.UserRepository;

public class SelfViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    // 昵称和账号
    private final MutableLiveData<String> avatarPath = new MutableLiveData<>(""); // 头像 URL 或路径
    private final MutableLiveData<String> nickname = new MutableLiveData<>("昵称");
    private final MutableLiveData<String> account = new MutableLiveData<>("账号：10000");

    public SelfViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public LiveData<String> getAccount() {
        return account;
    }

    public LiveData<String> getAvatar() {
        return avatarPath;
    }

    // 更新用户信息
    public void updateUserInfo(String newNickname, String newAccount, String newAvatar) {
        nickname.postValue(newNickname);
        account.postValue(newAccount);
        avatarPath.postValue(newAvatar);
    }

    public void updateSelfInfoFromLocal() {
        Integer userId = loadUserId();
        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile userProfile = userRepository.getUserProfileById(userId);
            updateUserInfo(userProfile.getNickname(), String.valueOf(userId), userProfile.getAvatarPath());
        });
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }
}
