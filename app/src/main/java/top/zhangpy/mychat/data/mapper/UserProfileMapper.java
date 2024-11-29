package top.zhangpy.mychat.data.mapper;

import android.content.Context;

import java.io.InputStream;

import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.util.StorageHelper;

public class UserProfileMapper {

    public static UserProfile mapToUserProfile(UserProfileModel userProfileModel, Context context) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(Integer.parseInt(userProfileModel.getUserId()));
        userProfile.setRegion(userProfileModel.getRegion());
        userProfile.setGender(userProfileModel.getGender());
        userProfile.setNickname(userProfileModel.getNickname());
        String avatarName = userProfileModel.getUserId() + "_avatar.jpg";
        InputStream avatar = StorageHelper.base64ToInputStream(userProfileModel.getAvatar());
        String path = StorageHelper.saveFile(context, "user", String.valueOf(userProfileModel.getUserId()), null, avatarName,avatar);
        userProfile.setAvatarPath(path);
        return userProfile;
    }

    public static UserProfileModel mapToUserProfileModel(UserProfile userProfile) {
        UserProfileModel userProfileModel = new UserProfileModel();
        userProfileModel.setUserId(String.valueOf(userProfile.getUserId()));
        userProfileModel.setRegion(userProfile.getRegion());
        userProfileModel.setGender(userProfile.getGender());
        userProfileModel.setNickname(userProfile.getNickname());
        userProfileModel.setAvatar(StorageHelper.inputStreamToBase64(userProfile.getAvatarPath()));
        return userProfileModel;
    }
}
