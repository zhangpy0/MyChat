package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.UserAccountMapper;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.data.service.MessageHandlerService;
import top.zhangpy.mychat.data.service.MessageReceiverService;
import top.zhangpy.mychat.util.HashGenerator;

@Getter
public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();

    private MutableLiveData<String> userIdOrEmail = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();
    private MutableLiveData<Boolean> showError = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void login() {
        if (userIdOrEmail.getValue() == null || password.getValue() == null) {
            showError.setValue(true);
            errorMessage.setValue("账号、密码不能为空");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            RequestMapModel requestMapModel = new RequestMapModel();
            requestMapModel.setUserId(userIdOrEmail.getValue());
            requestMapModel.setPasswordHash(HashGenerator.getPasswordHash(password.getValue()));
            try {
                UserAccountModel userAccountModel = userRepository.login(requestMapModel);
                User user = UserAccountMapper.mapToUser(userAccountModel);
                List<User> localUsers = userRepository.getAllUsers();
                requestMapModel.setFriendId(String.valueOf(user.getUserId()));
                UserProfileModel userProfileModel = userRepository.getUserProfile(user.getToken(), requestMapModel);
                UserProfile userProfile = UserProfileMapper.mapToUserProfile(userProfileModel, getApplication().getApplicationContext());
                UserProfile localUserProfile = userRepository.getUserProfileById(user.getUserId());
                if (localUsers.isEmpty()) {
                    userRepository.insertUser(user);
                    if (localUserProfile == null) {
                        userRepository.insertUserProfile(userProfile);
                    } else {
                        userRepository.updateUserProfile(userProfile);
                    }
                } else {
                    for (User localUser : localUsers) {
                        if (localUser.getUserId().equals(user.getUserId())) {
                            userRepository.updateUser(user);
                            userRepository.updateUserProfile(userProfile);
                        } else {
                            userRepository.deleteUser(localUser);
                        }
                    }
                }
                saveIdToken(getApplication().getApplicationContext(), user.getUserId(), userAccountModel.getToken());

                Intent serviceReceiverIntent = new Intent(this.getApplication(), MessageReceiverService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplication().getApplicationContext().startForegroundService(serviceReceiverIntent); // Android 8.0+ 启动前台服务
                } else {
                    getApplication().getApplicationContext().startService(serviceReceiverIntent); // 低版本直接启动服务
                }

                Intent serviceIntent = new Intent(this.getApplication(), MessageHandlerService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplication().getApplicationContext().startForegroundService(serviceIntent); // Android 8.0+ 启动前台服务
                } else {
                    getApplication().getApplicationContext().startService(serviceIntent); // 低版本直接启动服务
                }

                loginResult.postValue(true);
            } catch (IOException e) {
                showError.postValue(true);
                errorMessage.postValue("Network error " + e.getMessage());
            }
        });
    }

    private void saveIdToken(Context context, Integer userId , String token) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("user_id", userId).apply();
        prefs.edit().putString("auth_token", token).apply();
        prefs.edit().apply();
    }
}
