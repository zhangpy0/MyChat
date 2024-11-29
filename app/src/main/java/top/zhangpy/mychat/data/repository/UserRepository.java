package top.zhangpy.mychat.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zhangpy.mychat.data.exception.NetException;
import top.zhangpy.mychat.data.local.database.AppDatabase;
import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.remote.RetrofitClient;
import top.zhangpy.mychat.data.remote.api.ContactService;
import top.zhangpy.mychat.data.remote.api.UserAccountService;
import top.zhangpy.mychat.data.remote.api.UserCodeService;
import top.zhangpy.mychat.data.remote.api.UserProfileService;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;
import top.zhangpy.mychat.data.remote.model.UserAvatarModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;

public class UserRepository {
    private final AppDatabase database;

    private final UserCodeService userCodeService = RetrofitClient.userCodeService;

    private final UserAccountService userAccountService = RetrofitClient.userAccountService;

    private final UserProfileService userProfileService = RetrofitClient.userProfileService;

    private final ContactService contactService = RetrofitClient.contactService;

    public UserRepository() {
        this.database = null;
    }
    public UserRepository(Context context) {
        this.database = AppDatabase.getInstance(context, false);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return database.userDao().getAllUsers();
    }

    // 插入用户
    public void insertUser(User user) {
        database.userDao().insertUser(user);
    }

    // 更新用户
    public void updateUser(User user) {
        database.userDao().updateUser(user);
    }

    // 删除用户
    public void deleteUser(User user) {
        database.userDao().deleteUser(user);
    }

    public void insertUserProfile(UserProfile userProfile) {
        database.userProfileDao().insertUserProfile(userProfile);
    }

    public void updateUserProfile(UserProfile userProfile) {
        database.userProfileDao().updateUserProfile(userProfile);
    }

    public void deleteUserProfile(UserProfile userProfile) {
        database.userProfileDao().deleteUserProfile(userProfile);
    }

    public UserProfile getUserProfileById(Integer userId) {
        return database.userProfileDao().getUserProfileById(userId);
    }

    // 登录
    public UserAccountModel login(RequestMapModel requestMapModel) throws IOException {
        ResultModel<UserAccountModel> resultModel = userAccountService.login(requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public boolean register(RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userAccountService.register(requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean changePassword(RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userAccountService.changePassword(requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean sendEmailForRegister(RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userCodeService.sendEmailForRegister(requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean sendEmailForChangePassword(RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userCodeService.sendEmailForChangePassword(requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public UserProfileModel getUserProfile(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<UserProfileModel> resultModel = contactService.searchUser(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public UserAvatarModel getUserAvatar(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<UserAvatarModel> resultModel = userProfileService.getUserAvatar(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public boolean updateUserRegion(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userProfileService.updateUserRegion(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean updateUserNickname(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userProfileService.updateUserNickname(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean updateUserGender(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userProfileService.updateUserGender(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean updateUserAvatar(String token, String userId, String path) throws IOException {
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);
        ResultModel resultModel = userProfileService.updateUserAvatar(token, userId, body).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean check(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = userAccountService.check(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    private void saveToken(String token, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();
    }
}