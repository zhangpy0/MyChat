package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.data.repository.UserRepository;

public class ContactInfoViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final ContactRepository contactRepository;

    @Getter
    private final MutableLiveData<String> avatarPath = new MutableLiveData<>("");
    @Getter
    private final MutableLiveData<String> nickname = new MutableLiveData<>("未设置");
    @Getter
    private final MutableLiveData<String> account = new MutableLiveData<>("123456");
    @Getter
    private final MutableLiveData<String> gender = new MutableLiveData<>("未设置");
    @Getter
    private final MutableLiveData<String> region = new MutableLiveData<>("未设置");

    @Getter
    private final MutableLiveData<Boolean> updateResult = new MutableLiveData<>(false);

    public ContactInfoViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        contactRepository = new ContactRepository(application);
    }

    // Server -> sqlite -> UI
    public void updateUserInfoFromLocalAndServer(int friendId) {
        Integer userId = loadUserId();
        if (userId == -1) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String token = loadToken();
                if (token == null) {
                    return;
                }
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                requestMapModel.setFriendId(String.valueOf(friendId));
                UserProfileModel userProfileModel = userRepository.getUserProfile(token, requestMapModel);
                UserProfile oldUserProfile = userRepository.getUserProfileById(friendId);
                File oldAvatarFileDir = new File(oldUserProfile.getAvatarPath()).getParentFile();
                if (oldAvatarFileDir.exists()) {
                    File[] oldAvatarFiles = oldAvatarFileDir.listFiles();
                    if (oldAvatarFiles != null) {
                        for (File oldAvatarFile : oldAvatarFiles) {
                            if (oldAvatarFile.exists() && !oldAvatarFile.delete()) {
                                Log.e("ContactInfoViewModel", "updateUserInfoFromLocalAndServer: delete old avatar failed");
                            }
                        }
                    }
                }
                UserProfile userProfileServer = UserProfileMapper.mapToUserProfile(userProfileModel, getApplication().getApplicationContext());
                userRepository.updateUserProfile(userProfileServer);
                UserProfile userProfile = userRepository.getUserProfileById(friendId);
                if (userProfile != null) {
                    avatarPath.postValue(userProfile.getAvatarPath());
                    nickname.postValue(userProfile.getNickname());
                    account.postValue(String.valueOf(userProfile.getUserId()));
                    gender.postValue(Objects.equals(userProfile.getGender(), "male") ? "男" : "女");
                    region.postValue(userProfile.getRegion());
                    updateResult.postValue(true);
                } else {
                    Log.e("ContactInfoViewModel", "updateUserInfoFromLocalAndServer: user profile not match");
                }
            } catch (IOException e) {
                Log.e("ContactInfoViewModel", "updateUserInfoFromLocalAndServer: ", e);
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
