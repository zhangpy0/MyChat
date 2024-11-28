package top.zhangpy.mychat.data.remote.model;


import lombok.Data;

/*

Map<String, String> res = Map.of(
                "groupId", String.valueOf(groupInfo.getGroupId()),
                "groupName", groupInfo.getGroupName(),
                "announcement", groupInfo.getAnnouncement(),
                "avatar", avatarBase64
        );
 */
@Data
public class GroupInfoModel {
    private String groupId;
    private String groupName;
    private String announcement;
    private String avatar;
    private long createdAt;
    private String creatorId;
}
