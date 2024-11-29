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

public class EditGenderViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    @Getter
    private final MutableLiveData<Boolean> saveResult = new MutableLiveData<>(false);

    // 当前选择的性别
    private final MutableLiveData<String> selectedGender = new MutableLiveData<>("");

    public EditGenderViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<String> getSelectedGender() {
        return selectedGender;
    }

    public void setSelectedGender(String gender) {
        selectedGender.postValue(gender);
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    public void initGender() {
        Integer userId = loadUserId();
        if (userId == -1) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                if (userProfile.getGender().equals("male")) {
                    selectedGender.postValue("男");
                } else if (userProfile.getGender().equals("female")) {
                    selectedGender.postValue("女");
                }
            } catch (Exception e) {
                Log.e("EditGenderViewModel", "initGender: ", e);
            }
        });
    }

    public void updateGenderToLocalAndServer() {
        Integer userId = loadUserId();
        String token = loadToken();
        if (userId == -1 || token == null) {
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                String newGender = selectedGender.getValue().equals("男") ? "male" : "female";
                requestMapModel.setGender(newGender);
                userRepository.updateUserGender(token, requestMapModel);
                UserProfile userProfile = userRepository.getUserProfileById(userId);
                userProfile.setGender(newGender);
                userRepository.updateUserProfile(userProfile);
                saveResult.postValue(true);
                Log.d("EditGenderViewModel", "updateGenderToLocalAndServer: success");
            } catch (Exception e) {
                Log.e("EditGenderViewModel", "updateGenderToLocalAndServer: ", e);
            }
        });
    }
}
