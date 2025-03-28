package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;

public class FileViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;

    @Getter
    private final MutableLiveData<Boolean> isFileDownloaded = new MutableLiveData<>(false);

    @Getter
    private final MutableLiveData<Boolean> isDownloading = new MutableLiveData<>(false);

    @Getter
    private String filePath = null;

    public FileViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        Logger.initialize(application.getApplicationContext());
        Logger.enableLogging(true);
    }

    public boolean isFileDownloaded(int contactId, int messageId , String filePath) {
        String realPath = StorageHelper.getRealPathFromURI(getApplication(), filePath);
        if (realPath != null) {
            File file = new File(realPath);
            Logger.d("FileViewModel", "isFileDownloaded: realPath=" + realPath);
            return file.exists();
        }
        String tableName = ChatRepository.getTableName(contactId, loadUserId());
        ChatMessage chatMessage = chatRepository.getMessageById(tableName, messageId);
        String path = chatMessage.getFilePath();
        if (path != null) {
            File file = new File(path);
            this.filePath = path;
            Logger.d("FileViewModel", "isFileDownloaded: path=" + path);
            return file.exists();
        }
        return false;
    }

    public void downloadFile(int contactId, int messageId, String contactType) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String tableName;
            if ("user".equals(contactType)) {
                tableName = ChatRepository.getTableName(contactId, loadUserId());
            } else {
                tableName = ChatRepository.getTableName(contactId);
            }
            try {
                ChatMessage chatMessage = chatRepository.getMessageById(tableName, messageId);
                isDownloading.postValue(true);
                String path = chatRepository.downloadFile(getApplication(), String.valueOf(loadUserId()), loadToken(), chatMessage);
                Logger.d("FileViewModel", "downloadFile: path=" + path);
                isFileDownloaded.postValue(true);
                filePath = path;
            } catch (Exception e) {
                Logger.e("FileViewModel", "downloadFile: ", e);
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
