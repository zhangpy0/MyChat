package top.zhangpy.mychat.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import top.zhangpy.mychat.data.exception.NetException;
import top.zhangpy.mychat.data.local.database.AppDatabase;
import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;
import top.zhangpy.mychat.data.mapper.GroupMapper;
import top.zhangpy.mychat.data.remote.RetrofitClient;
import top.zhangpy.mychat.data.remote.api.GroupService;
import top.zhangpy.mychat.data.remote.model.GroupInfoModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.util.StorageHelper;

public class GroupRepository {

    private final AppDatabase database;

    private final GroupService groupService = RetrofitClient.groupService;

    public GroupRepository() {
        this.database = null;
    }

    public GroupRepository(Context context) {
        this.database = AppDatabase.getInstance(context, false);
    }

    public void insertGroup(Group group) {
        database.groupDao().insertGroup(group);
    }

    public void updateGroup(Group group) {
        database.groupDao().updateGroup(group);
    }

    public void deleteGroup(Group group) {
        database.groupDao().deleteGroup(group);
    }

    public Group getGroupById(Integer groupId) {
        return database.groupDao().getGroupById(groupId);
    }

    public List<Group> getAllGroupsSortedByMessageTime() {
        return database.groupDao().getAllGroupsSortedByMessageTime();
    }

    public GroupInfo getGroupInfoById(Integer groupId) {
        return database.groupDao().getGroupInfoById(groupId);
    }

    public void insertGroupInfo(GroupInfo groupInfo) {
        database.groupDao().insertGroupInfo(groupInfo);
    }

    public void updateGroupInfo(GroupInfo groupInfo) {
        database.groupDao().updateGroupInfo(groupInfo);
    }

    public void deleteGroupInfo(GroupInfo groupInfo) {
        database.groupDao().deleteGroupInfo(groupInfo);
    }


    public boolean updateGroupInfo(String token, @NonNull RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = groupService.updateGroupInfo(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean updateGroupAvatar(String token, String userId, String groupId, String path) throws IOException {
        File file = new File(path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);
        ResultModel resultModel = groupService.updateGroupAvatar(token, userId, groupId, body).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public GroupInfoModel getGroupInfo(String token, @NonNull RequestMapModel requestMapModel) throws IOException {
        ResultModel<GroupInfoModel> resultModel = groupService.getGroupInfo(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public boolean deleteGroup(String token, @NonNull RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = groupService.deleteGroup(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean createGroup(String token, @NonNull RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = groupService.createGroup(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public void updateGroupInfoFromServer(String token, Integer userId, Integer groupId, Context context) throws IOException {
        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        requestMapModel.setGroupId(String.valueOf(groupId));
        GroupInfoModel groupInfoModel = getGroupInfo(token, requestMapModel);
        GroupInfo oldGroupInfo = getGroupInfoById(groupId);
        if (oldGroupInfo == null) {
            GroupInfo newGroupInfo = GroupMapper.mapToGroupInfo(groupInfoModel, context);
            insertGroupInfo(newGroupInfo);
            return;
        }
        String oldAvatarBase64;
        if (oldGroupInfo.getAvatarPath() == null || oldGroupInfo.getAvatarPath().isEmpty()) {
            oldAvatarBase64 = "";
        } else {
            oldAvatarBase64 = StorageHelper.inputStreamToBase64(oldGroupInfo.getAvatarPath());
        }
        if (!oldAvatarBase64.equals(groupInfoModel.getAvatar())) {
            GroupInfo newGroupInfo = GroupMapper.mapToGroupInfo(groupInfoModel, context);
            updateGroupInfo(newGroupInfo);
        } else {
            GroupInfo newGroupInfo = new GroupInfo();
            newGroupInfo.setGroupId(groupId);
            newGroupInfo.setGroupName(groupInfoModel.getGroupName());
            newGroupInfo.setAnnouncement(groupInfoModel.getAnnouncement());
            updateGroupInfo(newGroupInfo);
        }
    }
}
