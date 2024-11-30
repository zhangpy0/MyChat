package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.ContactApply;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.ui.model.ApplyListItem;

public class AddFriendViewModel extends AndroidViewModel {

    @Getter
    private final MutableLiveData<Integer> updateStatus = new MutableLiveData<>();

    private final ContactRepository contactRepository;

    private final UserRepository userRepository;

    @Getter
    private final MutableLiveData<List<ApplyListItem>> applyList = new MutableLiveData<>();

    public AddFriendViewModel(@NonNull Application application) {
        super(application);
        contactRepository = new ContactRepository(application);
        userRepository = new UserRepository(application);
    }

    public void updateApplyListFromServer() {
        Integer userId = loadUserId();
        String token = loadToken();
        AtomicReference<List<ApplyListItem>> applies = new AtomicReference<>();
        List<ApplyListItem> applyList = null;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                applies.set(contactRepository.getApplyListFromServer(token, userId, getApplication()));
            } catch (Exception e) {
                Log.e("AddFriendViewModel", "updateApplyListFromServer: ", e);
            }
            this.applyList.postValue(applies.get());
        });
    }

    public void processFriendApply(int applyId, int key) {
        Integer userId = loadUserId();
        String token = loadToken();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ContactApply contactApply = contactRepository.getContactApplyById(applyId);
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                requestMapModel.setFriendId(String.valueOf(contactApply.getApplicantId()));
                requestMapModel.setStatus(key == 1 ? "1" : "0");
                contactRepository.processFriendRequest(token, requestMapModel);

                updateStatus.postValue(applyId);
            } catch (Exception e) {
                Log.e("AddFriendViewModel", "acceptApply: ", e);
            }
        });
    }

    public void processGroupApply(int applyId, int key) {
        Integer userId = loadUserId();
        String token = loadToken();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ContactApply contactApply = contactRepository.getContactApplyById(applyId);
                RequestMapModel requestMapModel = new RequestMapModel();
                requestMapModel.setUserId(String.valueOf(userId));
                requestMapModel.setOtherId(String.valueOf(contactApply.getApplicantId()));
                requestMapModel.setGroupId(String.valueOf(contactApply.getGroupId()));
                requestMapModel.setStatus(key == 1 ? "1" : "0");
                contactRepository.processGroupRequest(token, requestMapModel);

                updateStatus.postValue(applyId);
            } catch (Exception e) {
                Log.e("AddFriendViewModel", "acceptApply: ", e);
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
