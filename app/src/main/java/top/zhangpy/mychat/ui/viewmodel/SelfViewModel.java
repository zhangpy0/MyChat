package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
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

    public void updateSelfInfoFromLocalAndServer() {
        Integer userId = loadUserId();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String token = loadToken();
                if (token == null) {
                    return;
                }
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                requestMapModel.setFriendId(String.valueOf(userId));
                UserProfileModel userProfileModel = userRepository.getUserProfile(token, requestMapModel);
                UserProfile oldUserProfile = userRepository.getUserProfileById(userId);
                File oldAvatarFileDir = new File(oldUserProfile.getAvatarPath()).getParentFile();
                if (oldAvatarFileDir.exists()) {
                    File[] oldAvatarFiles = oldAvatarFileDir.listFiles();
                    if (oldAvatarFiles != null) {
                        for (File oldAvatarFile : oldAvatarFiles) {
                            if (oldAvatarFile.exists() && !oldAvatarFile.delete()) {
                                Log.e("PersonalInfoViewModel", "updateUserInfoFromLocalAndServer: delete old avatar failed");
                            }
                        }
                    }
                }
                UserProfile userProfileServer = UserProfileMapper.mapToUserProfile(userProfileModel, getApplication().getApplicationContext());
                userRepository.updateUserProfile(userProfileServer);
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                if (userProfile != null) {
                    updateUserInfo(userProfile.getNickname(), String.valueOf(userProfile.getUserId()), userProfile.getAvatarPath());
                } else {
                    Log.e("SelfViewModel", "updateSelfInfoFromLocalAndServer: user profile not match");
                }
            } catch (IOException e) {
                Log.e("SelfViewModel", "updateSelfInfoFromLocalAndServer: ", e);
            }
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
