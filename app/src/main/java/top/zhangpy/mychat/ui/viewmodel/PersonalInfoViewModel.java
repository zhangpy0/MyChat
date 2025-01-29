package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;

public class PersonalInfoViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    @Getter
    private final MutableLiveData<Boolean> updateResult = new MutableLiveData<>(false);

    /*
    已解决
    1. 性别修改：数据库local,server成功；UI失败 -> 全部成功
    2. 地区，昵称修改：数据库local,server失败；UI成功
    3. 头像修改：数据库local,server成功；UIself成功，UIpersonal需重新打开 -> 全部成功
    4. 头像文件生成过多 -> 已解决
     */

    private final MutableLiveData<String> avatarPath = new MutableLiveData<>(""); // 头像 URL 或路径
    private final MutableLiveData<String> nickname = new MutableLiveData<>("未设置");
    private final MutableLiveData<String> account = new MutableLiveData<>("123456");
    private final MutableLiveData<String> gender = new MutableLiveData<>("未设置");
    private final MutableLiveData<String> region = new MutableLiveData<>("未设置");

    public PersonalInfoViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        Logger.initialize(application.getApplicationContext());
        Logger.enableLogging(true);
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

    // Server -> sqlite -> UI
    public void updateUserInfoFromLocalAndServer() {
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
                requestMapModel.setFriendId(String.valueOf(userId));
                UserProfileModel userProfileModel = userRepository.getUserProfile(token, requestMapModel);
                UserProfile oldUserProfile = userRepository.getUserProfileById(userId);
                File oldAvatarFileDir = new File(oldUserProfile.getAvatarPath()).getParentFile();
                if (oldAvatarFileDir.exists()) {
                    File[] oldAvatarFiles = oldAvatarFileDir.listFiles();
                    if (oldAvatarFiles != null) {
                        for (File oldAvatarFile : oldAvatarFiles) {
                            if (oldAvatarFile.exists() && !oldAvatarFile.delete()) {
                                Logger.e("PersonalInfoViewModel", "updateUserInfoFromLocalAndServer: delete old avatar failed");
                            }
                        }
                    }
                }
                UserProfile userProfileServer = UserProfileMapper.mapToUserProfile(userProfileModel, getApplication().getApplicationContext());
                userRepository.updateUserProfile(userProfileServer);
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                if (userProfile != null) {
                    updateAvatar(userProfile.getAvatarPath());
                    updateNickname(userProfile.getNickname());
                    updateGender(userProfile.getGender());
                    updateRegion(userProfile.getRegion());
                    updateAccount(String.valueOf(userId));
                } else {
                    Logger.e("PersonalInfoViewModel", "updateUserInfoFromLocalAndServer: user profile not match");
                }
            } catch (IOException e) {
                Logger.e("PersonalInfoViewModel", "updateUserInfoFromLocalAndServer: ", e);
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
                updateResult.postValue(true);
            } catch (Exception e) {
                Logger.e("PersonalInfoViewModel", "updateToLocalAndServer: ", e);
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
                // 取个6位随机数，防止文件名重复
                String random = String.valueOf(System.currentTimeMillis()).substring(7);
                File newAvatar = new File(newAvatarDir, userId + "_avatar_" + random + ".jpg");
                if (newAvatar.exists() && !newAvatar.delete()) {
                    Logger.e("PersonalInfoViewModel", "updateUserAvatar: delete old avatar failed");
                    return;
                }
                File cachedAvatar = new File(newAvatarPath);
                if (!newAvatarDir.exists() && !newAvatarDir.mkdirs()) {
                    Logger.e("PersonalInfoViewModel", "updateUserAvatar: create new avatar directory failed");
                    return;
                }
                if (cachedAvatar.exists()) {
                    StorageHelper.copyFile(cachedAvatar, newAvatar);
                    userProfile.setAvatarPath(newAvatar.getAbsolutePath());
                }
                userRepository.updateUserProfile(userProfile);
                userRepository.updateUserAvatar(token, String.valueOf(userId), newAvatar.getAbsolutePath());
                newPath.set(newAvatar.getAbsolutePath());
                updateResult.postValue(true);
                Logger.d("PersonalInfoViewModel", "updateUserAvatar: success");
            } catch (Exception e) {
                Logger.e("PersonalInfoViewModel", "updateUserAvatar: ", e);
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
