package top.zhangpy.mychat.data.mapper;

import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;

public class UserAccountMapper {

    public static User mapToUser(UserAccountModel userAccountModel) {
        User user = new User();
        user.setUserId(Integer.parseInt(userAccountModel.getUserId()));
        user.setEmail(userAccountModel.getEmail());
        user.setPasswordHash(userAccountModel.getPasswordHash());
        user.setToken(userAccountModel.getToken());
        return user;
    }

    public static UserAccountModel mapToUserAccountModel(User user) {
        UserAccountModel userAccountModel = new UserAccountModel();
        userAccountModel.setUserId(String.valueOf(user.getUserId()));
        userAccountModel.setEmail(user.getEmail());
        userAccountModel.setPasswordHash(user.getPasswordHash());
        userAccountModel.setToken(user.getToken());
        return userAccountModel;
    }
}
