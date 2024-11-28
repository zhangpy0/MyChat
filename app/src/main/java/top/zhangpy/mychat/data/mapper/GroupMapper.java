package top.zhangpy.mychat.data.mapper;

import android.content.Context;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;

import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;
import top.zhangpy.mychat.data.remote.model.GroupInfoModel;
import top.zhangpy.mychat.util.Converter;
import top.zhangpy.mychat.util.StorageHelper;

public class GroupMapper {

    public static Group mapToGroup(GroupInfoModel groupInfoModel) {
        Group group = new Group();
        group.setGroupId(Integer.parseInt(groupInfoModel.getGroupId()));
        group.setCreatorId(Integer.parseInt(groupInfoModel.getCreatorId()));
        group.setCreatedAt(new Timestamp(groupInfoModel.getCreatedAt()));
        group.setMessageTime(Converter.fromUnixTimestamp(Instant.now().getEpochSecond()));
        return group;
    }

    public static GroupInfoModel mapToGroupInfoModel(Group group) {
        GroupInfoModel groupInfoModel = new GroupInfoModel();
        groupInfoModel.setGroupId(String.valueOf(group.getGroupId()));
        groupInfoModel.setCreatorId(String.valueOf(group.getCreatorId()));
        groupInfoModel.setCreatedAt(group.getCreatedAt().getTime());
        return groupInfoModel;
    }

    public static GroupInfo mapToGroupInfo(GroupInfoModel groupInfoModel, Context context) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(Integer.parseInt(groupInfoModel.getGroupId()));
        groupInfo.setGroupName(groupInfoModel.getGroupName());
        groupInfo.setAnnouncement(groupInfoModel.getAnnouncement());
        String avatarName = "group_avatar_" + groupInfoModel.getGroupId() + ".jpg";
        InputStream avatar = StorageHelper.base64ToInputStream(groupInfoModel.getAvatar());
        String path = StorageHelper.saveFile(context, "user", String.valueOf(groupInfoModel.getGroupId()), null, avatarName, avatar);
        groupInfo.setAvatarPath(path);
        return groupInfo;
    }
}
