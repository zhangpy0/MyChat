package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.util.Logger;

public class SettingsViewModel extends AndroidViewModel {

    private UserRepository userRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        Logger.initialize(application.getApplicationContext());
        Logger.enableLogging(true);
    }

    // 注销逻辑
    public void logout() {
        SharedPreferences.Editor editor = getApplication().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<User> users = userRepository.getAllUsers();
                for (User user : users) {
                    userRepository.deleteUser(user);
                }
            } catch (Exception e) {
                Logger.e("SettingsViewModel", "logout: ", e);
            }
        });
    }
}
