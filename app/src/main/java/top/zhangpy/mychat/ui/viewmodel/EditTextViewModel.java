package top.zhangpy.mychat.ui.viewmodel;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.repository.UserRepository;

public class EditTextViewModel extends AndroidViewModel {

    @Getter
    private final MutableLiveData<Boolean> updateResult = new MutableLiveData<>(false);

    private final UserRepository userRepository;

    private final MutableLiveData<String> text = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> showError = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public EditTextViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public LiveData<String> getText() {
        return text;
    }

    public void setText(String value) {
        text.setValue(value);
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    public void initText(int key) {
        // key 1: nickname 2: region
        Integer userId = loadUserId();
        String token = loadToken();
        if (userId == -1 || token == null) {
            showError.setValue(true);
            errorMessage.setValue("用户登录状况异常");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (key == 1) {
                    text.postValue(userRepository.getUserProfileById(userId).getNickname());
                } else if (key == 2) {
                    text.postValue(userRepository.getUserProfileById(userId).getRegion());
                }
            } catch (Exception e) {
                Log.e("EditTextViewModel", "initText: ", e);
            }
        });
    }

    public void updateToLocalAndServer(int key) {
        if (key == 0) {
            showError.setValue(true);
            errorMessage.setValue("标题异常");
            return;
        }
        // key 1: nickname 2: region
        Integer userId = loadUserId();
        String token = loadToken();
        if (userId == -1 || token == null) {
            showError.setValue(true);
            errorMessage.setValue("用户登录状况异常");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                if (key == 1) {
                    requestMapModel.setNickname(text.getValue());
                    userRepository.updateUserNickname(token, requestMapModel);
                    UserProfile userProfile = userRepository.getUserProfileById(userId);
                    userProfile.setNickname(text.getValue());
                    userRepository.updateUserProfile(userProfile);
                } else if (key == 2) {
                    requestMapModel.setRegion(text.getValue());
                    userRepository.updateUserRegion(token, requestMapModel);
                    UserProfile userProfile = userRepository.getUserProfileById(userId);
                    userProfile.setRegion(text.getValue());
                    userRepository.updateUserProfile(userProfile);
                }
                updateResult.postValue(true);
                Log.d("EditTextViewModel", "updateToLocalAndServer: success");
            } catch (Exception e) {
                Log.e("EditTextViewModel", "updateToLocalAndServer: ", e);
            }
        });
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
