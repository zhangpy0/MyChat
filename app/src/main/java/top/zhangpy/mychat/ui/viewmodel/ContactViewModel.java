package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.ui.model.ContactListItem;

public class ContactViewModel extends AndroidViewModel {

    private final ContactRepository contactRepository;

    @Getter
    private final MutableLiveData<List<ContactListItem>> contactList = new MutableLiveData<>();


    public ContactViewModel(@NonNull Application application) {
        super(application);
        contactRepository = new ContactRepository(application);
    }

    public void updateContactListFromServer() {
        Integer userId = loadUserId();
        String token = loadToken();
        AtomicReference<List<ContactListItem>> contacts = new AtomicReference<>();
        List<ContactListItem> contactList = null;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                contacts.set(contactRepository.getFriendListFromServer(token, userId));
            } catch (IOException e) {
                Log.e("ContactViewModel", "updateContactListFromServer: ", e);
            }
        });
        contactList = getInitialContactList();
        if (contacts.get() != null) {
            contactList.addAll(contacts.get());
        }
        this.contactList.postValue(contactList);
    }

    private Integer loadUserId() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private String loadToken() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    public List<ContactListItem> getInitialContactList() {
        List<ContactListItem> initialContactList = new ArrayList<>();
        initialContactList.add(new ContactListItem(-1, "新的朋友", "", "page"));
        initialContactList.add(new ContactListItem(-2, "群聊", "", "page"));
        initialContactList.add(new ContactListItem(-3, "标签", "", "page"));
        initialContactList.add(new ContactListItem(-4, "公众号", "", "page"));
        return initialContactList;
    }
}
