package top.zhangpy.mychat.data.remote.model;

import lombok.Data;

/*

Map<String, String> res = Map.of(
                "userId", String.valueOf(userProfile.getUserId()),
                "nickname", userProfile.getNickname(),
                "gender", userProfile.getGender(),
                "region", userProfile.getRegion()
        );

        Map<String, String> res = Map.of(
                "userId", String.valueOf(friendProfile.getUserId()),
                "nickname", friendProfile.getNickname(),
                "gender", friendProfile.getGender(),
                "region", friendProfile.getRegion(),
                "avatar", avatarBase64
        );
 */
@Data
public class UserProfileModel {
    private String userId;
    private String nickname;
    private String gender;
    private String region;
    private String avatar;
}
