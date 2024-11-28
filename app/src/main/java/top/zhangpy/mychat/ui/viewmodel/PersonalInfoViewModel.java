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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.util.StorageHelper;

public class PersonalInfoViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<String> avatarPath = new MutableLiveData<>(""); // 头像 URL 或路径
    private final MutableLiveData<String> nickname = new MutableLiveData<>("未设置");
    private final MutableLiveData<String> account = new MutableLiveData<>("123456");
    private final MutableLiveData<String> gender = new MutableLiveData<>("未设置");
    private final MutableLiveData<String> region = new MutableLiveData<>("未设置");

    public PersonalInfoViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<String> getAvatar() {
        return avatarPath;
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public LiveData<String> getAccount() {
        return account;
    }

    public LiveData<String> getGender() {
        return gender;
    }

    public LiveData<String> getRegion() {
        return region;
    }

    public void updateAvatar(String avatarUrl) {
        avatarPath.postValue(avatarUrl);

    }

    public void updateNickname(String newNickname) {
        nickname.postValue(newNickname);

    }

    public void updateAccount(String newAccount) {
        account.postValue(newAccount);

    }

    public void updateGender(String newGender) {
        if (newGender.equals("male")) {
            gender.postValue("男");
        } else if (newGender.equals("female")) {
            gender.postValue("女");
        } else {
            return;
        }

    }

    public void updateRegion(String newRegion) {
        region.postValue(newRegion);

    }

    public void updateUserInfoFromLocal() {
        Integer userId = loadUserId();
        if (userId == -1) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile userProfile = userRepository.getUserProfileById(userId);
            if (userProfile != null) {
                updateAvatar(userProfile.getAvatarPath());
                updateNickname(userProfile.getNickname());
                updateGender(userProfile.getGender());
                updateRegion(userProfile.getRegion());
                updateAccount(String.valueOf(userId));
            }
        });
    }

    public void updateToLocalAndServer() {
        Integer userId = loadUserId();
        String token = loadToken();
        if (userId == -1 || token == null) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                userProfile.setNickname(nickname.getValue());
                userProfile.setRegion(region.getValue());
                userProfile.setGender(Objects.equals(gender.getValue(), "男") ? "male" : "female");
                userProfile.setAvatarPath(avatarPath.getValue());
                userRepository.updateUserProfile(userProfile);
            } catch (Exception e) {
                Log.e("PersonalInfoViewModel", "updateToLocalAndServer: ", e);
            }
        });
    }

    public String updateUserAvatar(String newAvatarPath) {
        Integer userId = loadUserId();
        String token = loadToken();
        if (userId == -1 || token == null) {
            return null;
        }
        AtomicReference<String> newPath = new AtomicReference<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                File newAvatarDir = StorageHelper.getUserAvatarDirectory(getApplication().getApplicationContext(), String.valueOf(userId));
                File newAvatar = new File(newAvatarDir, userId + "_avatar.jpg");
                File cachedAvatar = new File(newAvatarPath);
                if (!newAvatarDir.exists() && !newAvatarDir.mkdirs()) {
                    return;
                }
                if (cachedAvatar.exists()) {
                    StorageHelper.copyFile(cachedAvatar, newAvatar);
                    userProfile.setAvatarPath(newAvatar.getAbsolutePath());
                }
                userRepository.updateUserProfile(userProfile);
                userRepository.updateUserAvatar(token, String.valueOf(userId), newAvatar.getAbsolutePath());
                newPath.set(newAvatar.getAbsolutePath());
            } catch (Exception e) {
                Log.e("PersonalInfoViewModel", "updateUserAvatar: ", e);
            }
        });
        return newPath.get();
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
