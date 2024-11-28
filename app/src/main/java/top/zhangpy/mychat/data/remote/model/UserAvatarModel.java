package top.zhangpy.mychat.data.remote.model;


import lombok.Data;

/*
Map<String, String> res = Map.of(
                "userId", String.valueOf(userProfile.getUserId()),
                "avatar", avatarBase64
        );
 */
@Data
public class UserAvatarModel {
    private String userId;
    private String avatar;
}
