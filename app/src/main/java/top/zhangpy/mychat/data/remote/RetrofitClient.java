package top.zhangpy.mychat.data.remote;

import static top.zhangpy.mychat.util.Constants.SERVER_IP;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import top.zhangpy.mychat.data.remote.api.ChatService;
import top.zhangpy.mychat.data.remote.api.ContactService;
import top.zhangpy.mychat.data.remote.api.GroupService;
import top.zhangpy.mychat.data.remote.api.UserAccountService;
import top.zhangpy.mychat.data.remote.api.UserCodeService;
import top.zhangpy.mychat.data.remote.api.UserProfileService;

public class RetrofitClient {
    private static final String BASE_URL = "http://" + SERVER_IP + ":8080/api/";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static UserCodeService userCodeService = retrofit.create(UserCodeService.class);
    public static UserAccountService userAccountService = retrofit.create(UserAccountService.class);
    public static UserProfileService userProfileService = retrofit.create(UserProfileService.class);
    public static GroupService groupService = retrofit.create(GroupService.class);
    public static ContactService contactService = retrofit.create(ContactService.class);
    public static ChatService chatService = retrofit.create(ChatService.class);
}
