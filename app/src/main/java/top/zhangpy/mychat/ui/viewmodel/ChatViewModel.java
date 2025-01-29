package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

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
import top.zhangpy.mychat.util.Logger;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;

    private ContactRepository contactRepository;

    private final UserRepository userRepository;

    @Getter
    private MutableLiveData<List<MessageListItem>> messages = new MutableLiveData<>();

    @Getter
    private MutableLiveData<Integer> isAvatarUpdated = new MutableLiveData<>(0);

    @Getter
    private MutableLiveData<String> friendName = new MutableLiveData<>("联系人");

    @Getter
    private MutableLiveData<String> myAvatarPath = new MutableLiveData<>(null);
    @Getter
    private MutableLiveData<String> friendAvatarPath = new MutableLiveData<>(null);


    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
        this.contactRepository = new ContactRepository(application);
        this.userRepository = new UserRepository(application);
        Logger.initialize(application.getApplicationContext());
        Logger.enableLogging(true);
    }

    public void loadMyAvatar() {
        Integer userId = loadUserId();
        Logger.d("ChatViewModel", "loadMyAvatar: " + userId);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String path = userRepository.getUserProfileById(userId).getAvatarPath();
                myAvatarPath.postValue(path); // 设置 LiveData
                Logger.d("ChatViewModel", "loadMyAvatar: " + path);
            } catch (Exception e) {
                Logger.e("ChatViewModel", "Failed to load my avatar", e);
            }
        });
    }

    public void loadFriendAvatar(Integer friendId) {
        Integer userId = loadUserId();
        String token = loadToken();
        Logger.d("ChatViewModel", "loadFriendAvatar: " + friendId);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserProfile friendProfile = userRepository.getUserProfileById(friendId);
                if (friendProfile == null || friendProfile.getAvatarPath() == null) {
                    userRepository.updateUserInfoFromServer(token, userId, friendId, getApplication());
                    friendProfile = userRepository.getUserProfileById(friendId);
                }
                friendAvatarPath.postValue(friendProfile.getAvatarPath()); // 设置 LiveData
                friendName.postValue(friendProfile.getNickname());
                Logger.d("ChatViewModel", "loadFriendAvatar: " + friendProfile.getAvatarPath());
            } catch (IOException e) {
                Logger.e("ChatViewModel", "Failed to load friend avatar", e);
            }
        });
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
                Logger.e("ChatViewModel", "Failed to get friend avatar", e);
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
        List<MessageListItem> messageListItems = null;
        try {
            messageListItems = chatRepository.updateMessagesFromLocal(userId, friendId, loadToken());
        } catch (IOException e) {
            Logger.e("ChatViewModel", "Failed to update messages", e);
        }
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
                Logger.e("ChatViewModel", "Failed to send message", e);
            }
        });
    }

    public void setAllMessagesRead(Integer friendId) {
        Integer userId = loadUserId();
        chatRepository.setAllMessagesRead(userId, friendId);
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
