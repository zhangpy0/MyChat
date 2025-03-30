package top.zhangpy.mychat.data.repository;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import top.zhangpy.mychat.data.exception.NetExceptionHandler;
import top.zhangpy.mychat.data.local.database.AppDatabase;
import top.zhangpy.mychat.data.local.entity.ContactApply;
import top.zhangpy.mychat.data.local.entity.Friend;
import top.zhangpy.mychat.data.local.entity.Group;
import top.zhangpy.mychat.data.local.entity.GroupInfo;
import top.zhangpy.mychat.data.local.entity.GroupMember;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.ContactApplyMapper;
import top.zhangpy.mychat.data.mapper.GroupMapper;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.RetrofitClient;
import top.zhangpy.mychat.data.remote.api.ContactService;
import top.zhangpy.mychat.data.remote.model.ContactApplyModel;
import top.zhangpy.mychat.data.remote.model.FriendModel;
import top.zhangpy.mychat.data.remote.model.GroupInfoModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.ui.model.ApplyListItem;
import top.zhangpy.mychat.ui.model.ContactListItem;
import top.zhangpy.mychat.util.Converter;

public class ContactRepository {

    private final Context context;

    private final AppDatabase database;

    private final ContactService contactService = RetrofitClient.contactService;

    private UserRepository userRepository;

    private GroupRepository groupRepository;

    public ContactRepository() {
        this.context = null;
        this.database = null;
        this.userRepository = new UserRepository();
        this.groupRepository = new GroupRepository();
    }

    public ContactRepository(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context, false);
        this.userRepository = new UserRepository(context);
        this.groupRepository = new GroupRepository(context);
    }

    public void insertContactApply(ContactApply contactApply) {
        database.contactDao().insertContactApply(contactApply);
    }

    private void insertContactApplies(List<ContactApply> newContactApplies) {
        database.contactDao().insertContactApplies(newContactApplies);
    }

    public void updateContactApply(ContactApply contactApply) {
        database.contactDao().updateContactApply(contactApply);
    }

    private void updateContactApplies(List<ContactApply> contactApplies) {
        database.contactDao().updateContactApplies(contactApplies);
    }

    public void deleteContactApply(ContactApply contactApply) {
        database.contactDao().deleteContactApply(contactApply);
    }

    public ContactApply getContactApplyById(Integer contactApplyId) {
        return database.contactDao().getContactApplyById(contactApplyId);
    }

    public List<ContactApply> getAllContactApplies() {
        return database.contactDao().getAllContactAppliesSortedByApplyTime();
    }

    public void insertFriend(Friend friend) {
        database.contactDao().insertFriend(friend);
    }

    public List<Friend> getFriendsByUserId(Integer userId) {
        return database.contactDao().getFriendsByUserId(userId);
    }

    public Friend getFriendByFriendId(Integer friendId) {
        return database.contactDao().getFriendByFriendId(friendId);
    }

    public void deleteFriend(Friend friend) {
        database.contactDao().deleteFriend(friend);
    }

    public void updateFriend(Friend friend) {
        database.contactDao().updateFriend(friend);
    }

    public List<Friend> getAllFriends() {
        return database.contactDao().getAllFriendsSortedByMessageTime();
    }

    public List<Friend> getAllFriendsSortedByMessageTime() {
        return getAllFriends();
    }

    public void insertGroupMember(GroupMember groupMember) {
        database.contactDao().insertGroupMember(groupMember);
    }

    public void updateGroupMember(GroupMember groupMember) {
        database.contactDao().updateGroupMember(groupMember);
    }

    public void deleteGroupMember(GroupMember groupMember) {
        database.contactDao().deleteGroupMember(groupMember);
    }

    public void deleteGroupMembersByGroupId(Integer groupId) {
        List<GroupMember> groupMembers = database.contactDao().getGroupMembersByGroupId(groupId);
        database.contactDao().deleteGroupMembers(groupMembers);
    }

    public List<GroupMember> getGroupMembersByGroupId(Integer groupId) {
        return database.contactDao().getGroupMembersByGroupId(groupId);
    }


    public boolean updateFriendStatus(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.updateFriendStatus(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean sendGroupRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.sendGroupRequest(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean sendFriendRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.sendFriendRequest(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public UserProfileModel searchUser(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<UserProfileModel> resultModel = contactService.searchUser(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel);
    }

    public GroupInfoModel searchGroup(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<GroupInfoModel> resultModel = contactService.searchGroup(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel);
    }

    public boolean processGroupRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.processGroupRequest(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean processFriendRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.processFriendRequest(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public List<Integer> getGroups(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<Map<String, String>>> resultModel = contactService.getGroups(token, requestMapModel.toMap()).execute().body();
        List<Map<String, String>> listMap = NetExceptionHandler.responseCheck(resultModel);
        List<Integer> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(Integer.parseInt(Objects.requireNonNull(map.get("groupId"))));
        }
        return list;
    }

    public Integer getGroupOwner(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<Map<String, String>> resultModel = contactService.getGroupOwner(token, requestMapModel.toMap()).execute().body();
        Map<String, String> map =  NetExceptionHandler.responseCheck(resultModel);
        return Integer.parseInt(Objects.requireNonNull(map.get("userId")));
    }

    public List<Integer> getGroupMembers(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<Map<String, String>>> resultModel = contactService.getGroupMembers(token, requestMapModel.toMap()).execute().body();
        List<Map<String, String >> listMap = NetExceptionHandler.responseCheck(resultModel);
        List<Integer> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(Integer.parseInt(Objects.requireNonNull(map.get("userId"))));
        }
        return list;
    }

    public List<FriendModel> getFriends(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<FriendModel>> resultModel = contactService.getFriends(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel);
    }

    public List<ContactApplyModel> getContactApplyFromOthers(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<ContactApplyModel>> resultModel = contactService.getContactApplyFromOthers(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel);
    }

    public List<ContactApplyModel> getContactApplyFromMe(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<ContactApplyModel>> resultModel = contactService.getContactApplyFromMe(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel);
    }

    public boolean deleteFriend(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.deleteFriend(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean deleteGroupMember(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.deleteFriendFromGroup(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean addGroupMember(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.addFriendToGroup(token, requestMapModel.toMap()).execute().body();
        return NetExceptionHandler.responseCheck(resultModel, 0);
    }

    public boolean updateContactApplyFromServer(String token, RequestMapModel requestMapModel) throws IOException {
        List<ContactApplyModel> contactApplyModels = getContactApplyFromOthers(token, requestMapModel);
        List<ContactApply> contactApplies = ContactApplyMapper.mapToContactApplyList(contactApplyModels);

        List<ContactApply> localContactApplies = getAllContactApplies();
        Set<ContactApply> localContactApplySet = new HashSet<>(localContactApplies);
//        for (ContactApply contactApply : contactApplies) {
//            boolean isExist = false;
//            for (ContactApply localContactApply : localContactApplies) {
//                if (contactApply.equals(localContactApply)) {
//                    isExist = true;
//                    break;
//                }
//            }
//            if (!isExist) {
//                insertContactApply(contactApply);
//            }
//        }
        List<ContactApply> newContactApplies = contactApplies.stream()
                .filter(contactApply -> !localContactApplySet.contains(contactApply))
                .collect(Collectors.toList());

        if (!newContactApplies.isEmpty()) {
            insertContactApplies(newContactApplies); // 批量插入
        }
        return true;
    }

    public boolean updateContactOfFriend(String token, RequestMapModel requestMapModel) throws IOException {
        List<FriendModel> friendModels = getFriends(token, requestMapModel);

        List<Friend> friends = new ArrayList<>();
        for (FriendModel friendModel : friendModels) {
            friends.add(friendModel.mapToFriend(Integer.parseInt(requestMapModel.getUserId())));
        }


        // 之前的好友也需要更新用户信息 已修改
        List<Friend> localFriends = getFriendsByUserId(Integer.parseInt(requestMapModel.getUserId()));
        Set<Friend> localFriendSet = new HashSet<>(localFriends);
        Set<Friend> friendSet = new HashSet<>(friends);
        List<Friend> newFriends = friends.stream()
                .filter(friend -> !localFriendSet.contains(friend))
                .collect(Collectors.toList());
        List<Friend> deleteFriends = localFriends.stream()
                .filter(friend -> !friendSet.contains(friend))
                .collect(Collectors.toList());
        for (Friend friend : newFriends) {
            try {
                insertFriend(friend);
                Integer friendId = friend.getFriendId();
                requestMapModel.setFriendId(String.valueOf(friendId));
                UserProfileModel userProfileModel = searchUser(token, requestMapModel);
                UserProfile userProfile = UserProfileMapper.mapToUserProfile(userProfileModel, context);
                database.userProfileDao().insertUserProfile(userProfile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (Friend friend : deleteFriends) {
            deleteFriend(friend);
        }
        List<Friend> allFriends = getAllFriends();
        for (Friend friend : allFriends) {
            Integer friendId = friend.getFriendId();
            UserProfile userProfile = userRepository.getUserProfileById(friendId);
            if (userProfile == null) {
                requestMapModel.setFriendId(String.valueOf(friendId));
                UserProfileModel userProfileModel = searchUser(token, requestMapModel);
                UserProfile newProfile = UserProfileMapper.mapToUserProfile(userProfileModel, context);
                userRepository.insertUserProfile(newProfile); // TODO 报错
            }
        }
        return true;
    }


    // TODO 群聊信息更新 同上
    public void updateContactOfGroup(String token, RequestMapModel requestMap) throws IOException {
        List<Integer> groupIds = getGroups(token, requestMap);

        List<Group> localGroups = database.groupDao().getAllGroupsSortedByMessageTime();
        Set<Integer> localGroupIdSet = localGroups.stream()
                .map(Group::getGroupId)
                .collect(Collectors.toSet());
        Set<Integer> groupIdSet = new HashSet<>(groupIds);
        List<Integer> newGroupIds = groupIds.stream()
                .filter(groupId -> !localGroupIdSet.contains(groupId))
                .collect(Collectors.toList());

        List<Integer> deleteGroupIds = localGroups.stream()
                .map(Group::getGroupId)
                .filter(groupId -> !groupIdSet.contains(groupId))
                .collect(Collectors.toList());

        for (Integer groupId : newGroupIds) {
            requestMap.setGroupId(String.valueOf(groupId));
            GroupInfoModel groupInfoModel = searchGroup(token, requestMap);
            GroupInfo groupInfo = GroupMapper.mapToGroupInfo(groupInfoModel, context);
            Group group = GroupMapper.mapToGroup(groupInfoModel);
            database.groupDao().insertGroup(group);
            database.groupDao().insertGroupInfo(groupInfo);

            Integer groupOwner = getGroupOwner(token, requestMap);
            List<Integer> groupMembers = getGroupMembers(token, requestMap);
            for (Integer userId : groupMembers) {
                GroupMember groupMember = new GroupMember();
                groupMember.setGroupId(groupId);
                groupMember.setUserId(userId);
                if (userId.equals(groupOwner)) {
                    groupMember.setRole("owner");
                } else {
                    groupMember.setRole("member");
                }
                database.contactDao().insertGroupMember(groupMember);
            }
        }

        for (Integer groupId : deleteGroupIds) {
            Group group = database.groupDao().getGroupById(groupId);
            GroupInfo groupInfo = database.groupDao().getGroupInfoById(groupId);
            List<GroupMember> groupMembers = database.contactDao().getGroupMembersByGroupId(groupId);
            database.groupDao().deleteGroup(group);
            database.groupDao().deleteGroupInfo(groupInfo);
            database.contactDao().deleteGroupMembers(groupMembers);
        }
    }

    public void updateFriendAndGroupFromServer(String token, Integer userId) throws IOException {
        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        updateContactOfFriend(token, requestMapModel);
        updateContactOfGroup(token, requestMapModel);
    }

    public List<ContactListItem> getFriendListFromServer(String token, Integer userId) throws IOException {
        updateFriendAndGroupFromServer(token, userId);
        List<Friend> friends = getFriendsByUserId(userId);
        List<ContactListItem> contactListItems = new ArrayList<>();
        for (Friend friend : friends) {
            UserProfile userProfile = database.userProfileDao().getUserProfileById(friend.getFriendId());
            contactListItems.add(new ContactListItem(friend.getFriendId(), userProfile.getNickname(), userProfile.getAvatarPath(), "user"));
        }
        contactListItems.sort(ContactListItem::compareTo);
        return contactListItems;
    }

    public List<ContactListItem> getGroupListFromServer(String token, Integer userId) throws IOException {
        updateFriendAndGroupFromServer(token, userId);
        List<Group> groups = database.groupDao().getAllGroupsSortedByMessageTime();
        List<ContactListItem> contactListItems = new ArrayList<>();
        for (Group group : groups) {
            GroupInfo groupInfo = database.groupDao().getGroupInfoById(group.getGroupId());
            contactListItems.add(new ContactListItem(group.getGroupId(), groupInfo.getGroupName(), groupInfo.getAvatarPath(), "group"));
        }
        contactListItems.sort(ContactListItem::compareTo);
        return contactListItems;
    }

//    public List<ApplyListItem> getApplyListFromServer(String token, Integer userId, Context context) throws IOException {
//        RequestMapModel requestMapModel = new RequestMapModel();
//        requestMapModel.setUserId(String.valueOf(userId));
//        List<ContactApplyModel> contactApplyModelsFromMe = getContactApplyFromMe(token, requestMapModel);
//        List<ContactApplyModel> contactApplyModelsFromOthers = getContactApplyFromOthers(token, requestMapModel);
//        // TODO 优化 本地数据更新
//        List<ContactApply> localContactApplies = getAllContactApplies();
//        Set<ContactApply> localContactApplySet = new HashSet<>(localContactApplies);
//        List<ApplyListItem> applyListItems = new ArrayList<>();
//        for (ContactApplyModel contactApplyModel : contactApplyModelsFromMe) {
//            ContactApply contactApply = ContactApplyMapper.mapToContactApply(contactApplyModel);
//            if (!localContactApplySet.contains(contactApply)) {
//                insertContactApply(contactApply);
//            } else {
//                ContactApply oldContactApply = localContactApplySet.stream()
//                        .filter(localContactApply -> localContactApply.equals(contactApply))
//                        .findFirst()
//                        .orElse(null);
//                if (oldContactApply != null && !oldContactApply.getStatus().equals(contactApply.getStatus())) {
//                    contactApply.setApplyId(oldContactApply.getApplyId());
//                    updateContactApply(contactApply);
//                }
//            }
//        }
//        for (ContactApplyModel contactApplyModel : contactApplyModelsFromOthers) {
//            ContactApply contactApply = ContactApplyMapper.mapToContactApply(contactApplyModel);
//            if (!localContactApplySet.contains(contactApply)) {
//                insertContactApply(contactApply);
//            } else {
//                ContactApply oldContactApply = localContactApplySet.stream()
//                        .filter(localContactApply -> localContactApply.equals(contactApply))
//                        .findFirst()
//                        .orElse(null);
//                if (oldContactApply != null && !oldContactApply.getStatus().equals(contactApply.getStatus())) {
//                    contactApply.setApplyId(oldContactApply.getApplyId());
//                    updateContactApply(contactApply);
//                }
//            }
//        }
//        List<ContactApply> contactApplies = getAllContactApplies();
//        for (ContactApply contactApply : contactApplies) {
//            Integer applicantId = contactApply.getApplicantId();
//            Integer receiverId = contactApply.getReceiverId();
//            Integer groupId = contactApply.getGroupId();
//            ApplyListItem applyListItem = new ApplyListItem();
//            if (receiverId.equals(userId)) {
//                if (contactApply.getContactType().equals("friend")) {
//                    userRepository.updateUserInfoFromServer(token, userId, applicantId, context);
//                    UserProfile userProfile = userRepository.getUserProfileById(applicantId);
//                    applyListItem.setId(contactApply.getApplyId());
//                    applyListItem.setAvatarPath(userProfile.getAvatarPath());
//                    applyListItem.setContactName(userProfile.getNickname());
//                    applyListItem.setSenderName(userProfile.getNickname());
//                    applyListItem.setType("friend");
//                    applyListItem.setContent(contactApply.getMessage());
//                    applyListItem.setStatus(contactApply.getStatus());
//                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
//                    applyListItem.setFlag(2);
//                    applyListItems.add(applyListItem);
//                } else {
//                    groupRepository.updateGroupInfoFromServer(token, userId, groupId, context);
//                    userRepository.updateUserInfoFromServer(token, userId, applicantId, context);
//                    GroupInfo groupInfo = groupRepository.getGroupInfoById(groupId);
//                    UserProfile userProfile = userRepository.getUserProfileById(applicantId);
//                    applyListItem.setId(contactApply.getApplyId());
//                    applyListItem.setAvatarPath(groupInfo.getAvatarPath());
//                    applyListItem.setContactName(groupInfo.getGroupName());
//                    applyListItem.setSenderName(userProfile.getNickname());
//                    applyListItem.setType("group");
//                    applyListItem.setContent(contactApply.getMessage());
//                    applyListItem.setStatus(contactApply.getStatus());
//                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
//                    applyListItem.setFlag(2);
//                    applyListItems.add(applyListItem);
//                }
//            } else if (applicantId.equals(userId)) {
//                if (contactApply.getContactType().equals("friend")) {
//                    userRepository.updateUserInfoFromServer(token, userId, receiverId, context);
//                    UserProfile userProfile = userRepository.getUserProfileById(receiverId);
//                    applyListItem.setId(contactApply.getApplyId());
//                    applyListItem.setAvatarPath(userProfile.getAvatarPath());
//                    applyListItem.setContactName(userProfile.getNickname());
//                    applyListItem.setSenderName(userProfile.getNickname());
//                    applyListItem.setType("friend");
//                    applyListItem.setContent(contactApply.getMessage());
//                    applyListItem.setStatus(contactApply.getStatus());
//                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
//                    applyListItem.setFlag(1);
//                    applyListItems.add(applyListItem);
//                } else {
//                    groupRepository.updateGroupInfoFromServer(token, userId, groupId, context);
//                    userRepository.updateUserInfoFromServer(token, userId, receiverId, context);
//                    GroupInfo groupInfo = groupRepository.getGroupInfoById(groupId);
//                    UserProfile userProfile = userRepository.getUserProfileById(receiverId);
//                    applyListItem.setId(contactApply.getApplyId());
//                    applyListItem.setAvatarPath(groupInfo.getAvatarPath());
//                    applyListItem.setContactName(groupInfo.getGroupName());
//                    applyListItem.setSenderName(userProfile.getNickname());
//                    applyListItem.setType("group");
//                    applyListItem.setContent(contactApply.getMessage());
//                    applyListItem.setStatus(contactApply.getStatus());
//                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
//                    applyListItem.setFlag(1);
//                    applyListItems.add(applyListItem);
//                }
//            }
//        }
//        return applyListItems;
//    }

    public List<ApplyListItem> getApplyListFromServer(String token, Integer userId, Context context) throws IOException {

        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));

        // 1. 合并远程申请数据（来自自己和他人）
        List<ContactApplyModel> fromMe = getContactApplyFromMe(token, requestMapModel);
        List<ContactApplyModel> fromOthers = getContactApplyFromOthers(token, requestMapModel);
        List<ContactApplyModel> allRemoteApplies = new ArrayList<>();
        allRemoteApplies.addAll(fromMe);
        allRemoteApplies.addAll(fromOthers);

        // 2. 转换为本地实体并分离需插入/更新的申请
        List<ContactApply> remoteApplies = ContactApplyMapper.mapToContactApplyList(allRemoteApplies);
        List<ContactApply> localApplies = getAllContactApplies();

        Set<ContactApply> localApplySet = new HashSet<>(localApplies);
        localApplies = localApplySet.stream()
                .filter(apply -> apply.getApplicantId().equals(userId) || apply.getReceiverId().equals(userId))
                .collect(Collectors.toList());

        List<ContactApply> toInsert = new ArrayList<>();
        List<ContactApply> toUpdate = new ArrayList<>();
        for (ContactApply remote : remoteApplies) {
            if (!localApplySet.contains(remote)) {
                toInsert.add(remote);
            } else {
                ContactApply local = localApplySet.stream()
                        .filter(apply -> apply.equals(remote))
                        .findFirst()
                        .orElse(null);
                if (local != null && !local.getStatus().equals(remote.getStatus())) {
                    remote.setApplyId(local.getApplyId());
                    toUpdate.add(remote);
                }
            }
        }

        // 3. 批量数据库操作
        if (!toInsert.isEmpty()) {
            insertContactApplies(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            updateContactApplies(toUpdate);
        }

        // 4. 收集所有关联的用户ID和群组ID
        Set<Integer> userIds = new HashSet<>();
        Set<Integer> groupIds = new HashSet<>();
        List<ContactApply> allApplies = getAllContactApplies(); // 获取最新数据
        for (ContactApply apply : allApplies) {
            boolean isFromMe = apply.getApplicantId().equals(userId);
            if (apply.getContactType().equals("friend")) {
                userIds.add(isFromMe ? apply.getReceiverId() : apply.getApplicantId());
            } else {
                groupIds.add(apply.getGroupId());
                userIds.add(isFromMe ? apply.getReceiverId() : apply.getApplicantId());
            }
        }

        List<Integer> usersToFetch = new ArrayList<>();
        for (Integer id : userIds) {
            UserProfile profile = userRepository.getUserProfileById(id);
            boolean needUpdate = profile == null || !isAvatarValid(profile.getAvatarPath());
            if (needUpdate) {
                usersToFetch.add(id);
            }
        }

        List<Integer> groupsToFetch = new ArrayList<>();
        for (Integer id : groupIds) {
            GroupInfo info = groupRepository.getGroupInfoById(id);
            boolean needUpdate = info == null || !isAvatarValid(info.getAvatarPath());
            if (needUpdate) {
                groupsToFetch.add(id);
            }
        }

        userRepository.batchFetchAndCacheUsers(token, userId, usersToFetch, context);
        groupRepository.batchFetchAndCacheGroups(token, userId, groupsToFetch, context);

        List<ApplyListItem> applyListItems = new ArrayList<>();
        for (ContactApply apply : allApplies) {
            boolean isFromMe = apply.getApplicantId().equals(userId);
            ApplyListItem item = new ApplyListItem();
            item.setId(apply.getApplyId());
            item.setContent(apply.getMessage());
            item.setStatus(apply.getStatus());
            item.setAbsTime(Converter.dateToTimestamp(apply.getApplyTime()));
            item.setFlag(isFromMe ? 1 : 2);

            if (apply.getContactType().equals("friend")) {
                int targetUserId = isFromMe ? apply.getReceiverId() : apply.getApplicantId();
                UserProfile profile = userRepository.getUserProfileById(targetUserId);
                if (profile != null) {
                    item.setType("friend");
                    item.setContactName(profile.getNickname());
                    item.setAvatarPath(profile.getAvatarPath());
                    item.setSenderName(profile.getNickname());
                }
            } else {
                GroupInfo groupInfo = groupRepository.getGroupInfoById(apply.getGroupId());
                int senderId = isFromMe ? apply.getReceiverId() : apply.getApplicantId();
                UserProfile sender = userRepository.getUserProfileById(senderId);
                if (groupInfo != null && sender != null) {
                    item.setType("group");
                    item.setContactName(groupInfo.getGroupName());
                    item.setAvatarPath(groupInfo.getAvatarPath());
                    item.setSenderName(sender.getNickname());
                }
            }
            applyListItems.add(item);
        }

        applyListItems.sort(ApplyListItem::compareTo);
        return applyListItems;

    }

    // 新增文件有效性检查方法
    private boolean isAvatarValid(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty()) {
            return false; // 无头像路径需要更新
        }

        try {
            File avatarFile = new File(avatarPath);
            if (!avatarFile.exists()) {
                return false; // 文件不存在需要更新
            }

            // 检查最后修改时间是否在7天内
            long lastModified = avatarFile.lastModified();
            long sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
            return lastModified >= sevenDaysAgo;
        } catch (SecurityException e) {
            Log.e("FileCheck", "No permission to access avatar file: " + avatarPath);
            return false; // 无权限访问视为需要更新
        }
    }

    public boolean isMyFriend(int userId,int friendId) throws IOException {
        List<Friend> friends = getFriendsByUserId(userId);
        for (Friend friend : friends) {
            if (friend.getFriendId().equals(friendId)) {
                return true;
            }
        }
        return false;
    }
}
