package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.ui.model.ChatListItem;

@Getter
public class WeixinViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;

    private final ContactRepository contactRepository;

    private final MutableLiveData<List<ChatListItem>> messages = new MutableLiveData<>();

    public WeixinViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.contactRepository = new ContactRepository(application);
    }

    public void updateMessages() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String token = loadToken();
            Integer userId = loadUserId();
            try {
                contactRepository.updateFriendAndGroupFromServer(token, userId);
            } catch (IOException e) {
                Log.e("WeixinViewModel", "updateMessages: ", e);
            }
            List<ChatListItem> chatListItemList = chatRepository.updateChatListFromLocal();
            messages.postValue(chatListItemList);
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