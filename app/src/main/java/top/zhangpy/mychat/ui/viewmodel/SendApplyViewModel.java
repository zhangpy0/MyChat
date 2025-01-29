package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;

import java.io.IOException;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.util.Logger;

public class SendApplyViewModel extends AndroidViewModel {

    private final ContactRepository contactRepository;

    public SendApplyViewModel(Application application) {
        super(application);
        contactRepository = new ContactRepository(application);
        Logger.initialize(application.getApplicationContext());
        Logger.enableLogging(true);
    }

    public void sendApply(int friendId, String content) {
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
                requestMapModel.setMessage(content);
                boolean isSend = contactRepository.sendFriendRequest(token, requestMapModel);
                if (!isSend) {
                    Logger.e("SendApplyViewModel", "sendApply: 发送好友请求失败");
                }
            } catch (IOException e) {
                Logger.e("SendApplyViewModel", "sendApply: ", e);
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
