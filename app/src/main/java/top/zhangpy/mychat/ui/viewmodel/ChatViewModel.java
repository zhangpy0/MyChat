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
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.ui.model.MessageListItem;

public class ChatViewModel extends AndroidViewModel {

    private ChatRepository chatRepository;

    private ContactRepository contactRepository;

    private UserRepository userRepository;

    @Getter
    private MutableLiveData<List<MessageListItem>> messages = new MutableLiveData<>();

    @Getter
    private MutableLiveData<Integer> isAvatarUpdated = new MutableLiveData<>(0);

    @Getter
    private MutableLiveData<String> friendName = new MutableLiveData<>("联系人");

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.contactRepository = new ContactRepository(application);
        this.userRepository = new UserRepository(application);
    }

    public String getFriendAvatar(Integer friendId) {
        Integer userId = loadUserId();
        String token = loadToken();
        AtomicReference<String> avatar = new AtomicReference<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                userRepository.updateUserInfoFromServer(token, userId, friendId, getApplication());
                UserProfile friendProfile = userRepository.getUserProfileById(friendId);
                avatar.set(friendProfile.getAvatarPath());
                friendName.postValue(friendProfile.getNickname());
                Integer updated = isAvatarUpdated.getValue();
                isAvatarUpdated.postValue(updated + 1);
            } catch (IOException e) {
                Log.e("ChatViewModel", "Failed to get friend avatar", e);
            }
        });
        return avatar.get();
    }

    public String getMyAvatar() {
        Integer userId = loadUserId();
        AtomicReference<String> path = new AtomicReference<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            path.set(userRepository.getUserProfileById(userId).getAvatarPath());
            Integer updated = isAvatarUpdated.getValue();
            isAvatarUpdated.postValue(updated + 1);
        });
        return path.get();
    }

    public void updateMessagesFromLocal(Integer friendId) {
        Integer userId = loadUserId();
        List<MessageListItem> messageListItems = chatRepository.updateMessagesFromLocal(userId, friendId);
        messages.postValue(messageListItems);
    }

    public void sendMessageToFriend(Integer friendId, String content, String messageType, String filePath) {
        Integer userId = loadUserId();
        String token = loadToken();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                chatRepository.sendMessageToServer(userId, friendId, content, messageType, token, filePath);
                updateMessagesFromLocal(friendId);
            } catch (IOException e) {
                Log.e("ChatViewModel", "Failed to send message", e);
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