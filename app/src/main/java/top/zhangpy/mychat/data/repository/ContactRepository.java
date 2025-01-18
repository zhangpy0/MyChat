package top.zhangpy.mychat.data.repository;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import top.zhangpy.mychat.data.exception.NetException;
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
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean sendGroupRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.sendGroupRequest(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean sendFriendRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.sendFriendRequest(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public UserProfileModel searchUser(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<UserProfileModel> resultModel = contactService.searchUser(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public GroupInfoModel searchGroup(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<GroupInfoModel> resultModel = contactService.searchGroup(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public boolean processGroupRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.processGroupRequest(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean processFriendRequest(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.processFriendRequest(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public List<Integer> getGroups(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<Map<String, String>>> resultModel = contactService.getGroups(token, requestMapModel.toMap()).execute().body();
        List<Map<String, String>> listMap = NetException.responseCheck(resultModel);
        List<Integer> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(Integer.parseInt(Objects.requireNonNull(map.get("groupId"))));
        }
        return list;
    }

    public Integer getGroupOwner(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<Map<String, String>> resultModel = contactService.getGroupOwner(token, requestMapModel.toMap()).execute().body();
        Map<String, String> map =  NetException.responseCheck(resultModel);
        return Integer.parseInt(Objects.requireNonNull(map.get("userId")));
    }

    public List<Integer> getGroupMembers(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<Map<String, String>>> resultModel = contactService.getGroupMembers(token, requestMapModel.toMap()).execute().body();
        List<Map<String, String >> listMap = NetException.responseCheck(resultModel);
        List<Integer> list = new ArrayList<>();
        for (Map<String, String> map : listMap) {
            list.add(Integer.parseInt(Objects.requireNonNull(map.get("userId"))));
        }
        return list;
    }

    public List<FriendModel> getFriends(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<FriendModel>> resultModel = contactService.getFriends(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public List<ContactApplyModel> getContactApplyFromOthers(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<ContactApplyModel>> resultModel = contactService.getContactApplyFromOthers(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public List<ContactApplyModel> getContactApplyFromMe(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel<List<ContactApplyModel>> resultModel = contactService.getContactApplyFromMe(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel);
    }

    public boolean deleteFriend(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.deleteFriend(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean deleteGroupMember(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.deleteFriendFromGroup(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
    }

    public boolean addGroupMember(String token, RequestMapModel requestMapModel) throws IOException {
        ResultModel resultModel = contactService.addFriendToGroup(token, requestMapModel.toMap()).execute().body();
        return NetException.responseCheck(resultModel, 0);
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
        return true;
    }


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

    public List<ApplyListItem> getApplyListFromServer(String token, Integer userId, Context context) throws IOException {
        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        List<ContactApplyModel> contactApplyModelsFromMe = getContactApplyFromMe(token, requestMapModel);
        List<ContactApplyModel> contactApplyModelsFromOthers = getContactApplyFromOthers(token, requestMapModel);
        // TODO 优化 本地数据更新
        List<ContactApply> localContactApplies = getAllContactApplies();
        Set<ContactApply> localContactApplySet = new HashSet<>(localContactApplies);
        List<ApplyListItem> applyListItems = new ArrayList<>();
        for (ContactApplyModel contactApplyModel : contactApplyModelsFromMe) {
            ContactApply contactApply = ContactApplyMapper.mapToContactApply(contactApplyModel);
            if (!localContactApplySet.contains(contactApply)) {
                insertContactApply(contactApply);
            } else {
                ContactApply oldContactApply = localContactApplySet.stream()
                        .filter(localContactApply -> localContactApply.equals(contactApply))
                        .findFirst()
                        .orElse(null);
                if (oldContactApply != null && !oldContactApply.getStatus().equals(contactApply.getStatus())) {
                    contactApply.setApplyId(oldContactApply.getApplyId());
                    updateContactApply(contactApply);
                }
            }
        }
        for (ContactApplyModel contactApplyModel : contactApplyModelsFromOthers) {
            ContactApply contactApply = ContactApplyMapper.mapToContactApply(contactApplyModel);
            if (!localContactApplySet.contains(contactApply)) {
                insertContactApply(contactApply);
            } else {
                ContactApply oldContactApply = localContactApplySet.stream()
                        .filter(localContactApply -> localContactApply.equals(contactApply))
                        .findFirst()
                        .orElse(null);
                if (oldContactApply != null && !oldContactApply.getStatus().equals(contactApply.getStatus())) {
                    contactApply.setApplyId(oldContactApply.getApplyId());
                    updateContactApply(contactApply);
                }
            }
        }
        List<ContactApply> contactApplies = getAllContactApplies();
        for (ContactApply contactApply : contactApplies) {
            Integer applicantId = contactApply.getApplicantId();
            Integer receiverId = contactApply.getReceiverId();
            Integer groupId = contactApply.getGroupId();
            ApplyListItem applyListItem = new ApplyListItem();
            if (receiverId.equals(userId)) {
                if (contactApply.getContactType().equals("friend")) {
                    userRepository.updateUserInfoFromServer(token, userId, applicantId, context);
                    UserProfile userProfile = userRepository.getUserProfileById(applicantId);
                    applyListItem.setId(contactApply.getApplyId());
                    applyListItem.setAvatarPath(userProfile.getAvatarPath());
                    applyListItem.setContactName(userProfile.getNickname());
                    applyListItem.setSenderName(userProfile.getNickname());
                    applyListItem.setType("friend");
                    applyListItem.setContent(contactApply.getMessage());
                    applyListItem.setStatus(contactApply.getStatus());
                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
                    applyListItem.setFlag(2);
                    applyListItems.add(applyListItem);
                } else {
                    groupRepository.updateGroupInfoFromServer(token, userId, groupId, context);
                    userRepository.updateUserInfoFromServer(token, userId, applicantId, context);
                    GroupInfo groupInfo = groupRepository.getGroupInfoById(groupId);
                    UserProfile userProfile = userRepository.getUserProfileById(applicantId);
                    applyListItem.setId(contactApply.getApplyId());
                    applyListItem.setAvatarPath(groupInfo.getAvatarPath());
                    applyListItem.setContactName(groupInfo.getGroupName());
                    applyListItem.setSenderName(userProfile.getNickname());
                    applyListItem.setType("group");
                    applyListItem.setContent(contactApply.getMessage());
                    applyListItem.setStatus(contactApply.getStatus());
                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
                    applyListItem.setFlag(2);
                    applyListItems.add(applyListItem);
                }
            } else if (applicantId.equals(userId)) {
                if (contactApply.getContactType().equals("friend")) {
                    userRepository.updateUserInfoFromServer(token, userId, receiverId, context);
                    UserProfile userProfile = userRepository.getUserProfileById(receiverId);
                    applyListItem.setId(contactApply.getApplyId());
                    applyListItem.setAvatarPath(userProfile.getAvatarPath());
                    applyListItem.setContactName(userProfile.getNickname());
                    applyListItem.setSenderName(userProfile.getNickname());
                    applyListItem.setType("friend");
                    applyListItem.setContent(contactApply.getMessage());
                    applyListItem.setStatus(contactApply.getStatus());
                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
                    applyListItem.setFlag(1);
                    applyListItems.add(applyListItem);
                } else {
                    groupRepository.updateGroupInfoFromServer(token, userId, groupId, context);
                    userRepository.updateUserInfoFromServer(token, userId, receiverId, context);
                    GroupInfo groupInfo = groupRepository.getGroupInfoById(groupId);
                    UserProfile userProfile = userRepository.getUserProfileById(receiverId);
                    applyListItem.setId(contactApply.getApplyId());
                    applyListItem.setAvatarPath(groupInfo.getAvatarPath());
                    applyListItem.setContactName(groupInfo.getGroupName());
                    applyListItem.setSenderName(userProfile.getNickname());
                    applyListItem.setType("group");
                    applyListItem.setContent(contactApply.getMessage());
                    applyListItem.setStatus(contactApply.getStatus());
                    applyListItem.setAbsTime(Converter.dateToTimestamp(contactApply.getApplyTime()));
                    applyListItem.setFlag(1);
                    applyListItems.add(applyListItem);
                }
            }
        }
        return applyListItems;
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
