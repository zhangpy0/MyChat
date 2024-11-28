package top.zhangpy.mychat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import top.zhangpy.mychat.data.mapper.ServerMessageMapper;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.ContactApplyModel;
import top.zhangpy.mychat.data.remote.model.DownloadModel;
import top.zhangpy.mychat.data.remote.model.FriendModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ServerMessageModel;
import top.zhangpy.mychat.data.remote.model.UserAvatarModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.ContactRepository;
import top.zhangpy.mychat.data.repository.GroupRepository;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.util.HashGenerator;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    private final UserRepository userRepository = new UserRepository();

    private final GroupRepository groupRepository = new GroupRepository();

    private final ContactRepository contactRepository = new ContactRepository();

    private final ChatRepository chatRepository = new ChatRepository();

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void UserTest() throws IOException {

        Integer userId = 11113;
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMyIsImlhdCI6MTczMjEwMjQ0NiwiZXhwIjoxNzMyNzA3MjQ2fQ.USt5psOImqFF49o2ANaYuMtx6OTiNRMpKm8pnV1QKjs";
        String email = "1304152517@qq.com";
        String passwordHash = "12345";
        String newPasswordHash = "12345";

        String authCode = "540895";

        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        requestMapModel.setPasswordHash(passwordHash);
        requestMapModel.setEmail(email);
        requestMapModel.setAuthCode(authCode);
        requestMapModel.setNewPasswordHash(newPasswordHash);


//        boolean isSendAuthCode = userRepository.sendEmailForChangePassword(requestMapModel);
//        assertTrue(isSendAuthCode);
//        System.out.println("发送验证码成功");

//        boolean isRegister = userRepository.register(requestMapModel);
//        assertTrue(isRegister);
//        System.out.println("注册成功");

//        boolean isChangePassword = userRepository.changePassword(requestMapModel);
//        assertTrue(isChangePassword);
//        System.out.println("修改密码成功");

//        UserAccountModel userAccountModel = userRepository.login(requestMapModel);
//        assertEquals(userAccountModel.getUserId(), String.valueOf(userId));
//        System.out.println(userAccountModel);

        UserProfileModel userProfileModel = userRepository.getUserProfile(token, requestMapModel);
        assertEquals(userProfileModel.getUserId(), String.valueOf(userId));
        System.out.println(userProfileModel);

        requestMapModel.setRegion("China");
        boolean isUpdateUserRegion = userRepository.updateUserRegion(token, requestMapModel);
        assertTrue(isUpdateUserRegion);
        System.out.println("修改用户地区成功");

        requestMapModel.setNickname("test3");
        boolean isUpdateUserNickname = userRepository.updateUserNickname(token, requestMapModel);
        assertTrue(isUpdateUserNickname);
        System.out.println("修改用户昵称成功");

        requestMapModel.setGender("male");
        boolean isUpdateUserGender = userRepository.updateUserGender(token, requestMapModel);
        assertTrue(isUpdateUserGender);
        System.out.println("修改用户性别成功");

        String path = "D:/IT/Project/MyChat/file/user/11111/dog.jpg";
        boolean isUpdateUserAvatar = userRepository.updateUserAvatar(token, String.valueOf(userId), path);
        assertTrue(isUpdateUserAvatar);
        System.out.println("修改用户头像成功");

        UserAvatarModel userAvatarModel = userRepository.getUserAvatar(token, requestMapModel);
        assertEquals(userAvatarModel.getUserId(), String.valueOf(userId));
        System.out.println(userAvatarModel);

        userProfileModel = userRepository.getUserProfile(token, requestMapModel);
        assertEquals(userProfileModel.getUserId(), String.valueOf(userId));
        System.out.println(userProfileModel);
    }

    @Test
    public void GroupTest() throws IOException {
        Integer userId = 11113;
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMyIsImlhdCI6MTczMjEwMjQ0NiwiZXhwIjoxNzMyNzA3MjQ2fQ.USt5psOImqFF49o2ANaYuMtx6OTiNRMpKm8pnV1QKjs";


        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));

//        boolean isCreateGroup = groupRepository.createGroup(token, requestMapModel);
//        assertTrue(isCreateGroup);
//        System.out.println("创建群组成功");

        List<Integer> groups = contactRepository.getGroups(token, requestMapModel);
        assertFalse(groups.isEmpty());
        Integer groupId = groups.get(2);
        System.out.println(groupId);
        System.out.println("获取群组成功");

        requestMapModel.setGroupId(String.valueOf(groupId));
//        Integer groupOwner = contactRepository.getGroupOwner(token, requestMapModel);
//        requestMapModel.setFriendId(String.valueOf(groupOwner));
//        UserProfileModel owner = contactRepository.searchUser(token, requestMapModel);
//        System.out.println(owner);



        requestMapModel.setFriendId("11111");
        UserProfileModel userProfileModel = contactRepository.searchUser(token, requestMapModel);
        System.out.println(userProfileModel);

//        requestMapModel.setMessage("test");
//        boolean isSendFriendRequest = contactRepository.sendFriendRequest(token, requestMapModel);
//        assertTrue(isSendFriendRequest);

//        boolean isAddGroupMember = contactRepository.addGroupMember(token, requestMapModel);
//        assertTrue(isAddGroupMember);
//        System.out.println("添加群成员成功");

//        boolean isDeleteGroupMember = contactRepository.deleteGroupMember(token, requestMapModel);
//        assertTrue(isDeleteGroupMember);
//        System.out.println("删除群成员成功");

        List<Integer> groupMembers = contactRepository.getGroupMembers(token, requestMapModel);
        assertFalse(groupMembers.isEmpty());
        System.out.println(groupMembers);

        List<ContactApplyModel> contactApplyFromOthers = contactRepository.getContactApplyFromOthers(token, requestMapModel);
        System.out.println(contactApplyFromOthers);

        requestMapModel.setOtherId("11111");
        requestMapModel.setStatus("1");
        boolean isProcessGroupRequest = contactRepository.processGroupRequest(token, requestMapModel);
        assertTrue(isProcessGroupRequest);
        System.out.println("处理群组请求成功");
    }

    @Test
    public void ContactTest() throws IOException {
        Integer userId = 11111;
        String passwordHash = "12345";
        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        requestMapModel.setPasswordHash(passwordHash);
//        UserAccountModel userAccountModel = userRepository.login(requestMapModel);
//        String token = userAccountModel.getToken();
//        System.out.println(token);
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMSIsImlhdCI6MTczMjE2MzgyOSwiZXhwIjoxNzMyNzY4NjI5fQ.HE34u4UEsGf2lw7uq8e_9n279pUQK81AM8s9KaT2wkk";

        List<ContactApplyModel> contactApplyFromOthers = contactRepository.getContactApplyFromOthers(token, requestMapModel);
        System.out.println(contactApplyFromOthers);

//        requestMapModel.setFriendId("11113");
//        requestMapModel.setStatus("1");
//        boolean isProcessFriendRequest = contactRepository.processFriendRequest(token, requestMapModel);
//        assertTrue(isProcessFriendRequest);
//        System.out.println("处理好友请求成功");

        List<FriendModel> friends = contactRepository.getFriends(token, requestMapModel);
        System.out.println(friends);

        requestMapModel.setGroupId("1000010");
        boolean isSendGroupRequest = contactRepository.sendGroupRequest(token, requestMapModel);
        assertTrue(isSendGroupRequest);
        System.out.println("发送群组请求成功");

        List<ContactApplyModel> contactApplyFromMe = contactRepository.getContactApplyFromMe(token, requestMapModel);
        System.out.println(contactApplyFromMe);

    }

    @Test
    public void chatTest1() throws IOException {
        Integer userId = 11111;
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMSIsImlhdCI6MTczMjE2MzgyOSwiZXhwIjoxNzMyNzY4NjI5fQ.HE34u4UEsGf2lw7uq8e_9n279pUQK81AM8s9KaT2wkk";
        Integer receiverId = 11113;
        Integer groupId = 1000010;
        String content = "test3";

        String receiverType = "user";
//        String receiverType = "group";
        String messageType = "image";
        String path = "D:/IT/Project/MyChat/file/user/11111/dog.jpg";

        boolean isSendChatMessage = chatRepository.sendMessage(String.valueOf(userId), String.valueOf(receiverId), String.valueOf(groupId), receiverType, content, messageType, token, path);
        assertTrue(isSendChatMessage);
        System.out.println("发送消息成功");
    }

    @Test
    public void chatTest2() throws IOException {
        Integer userId = 11111;
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMSIsImlhdCI6MTczMjE2MzgyOSwiZXhwIjoxNzMyNzY4NjI5fQ.HE34u4UEsGf2lw7uq8e_9n279pUQK81AM8s9KaT2wkk";
        Integer receiverId = 0;
        Integer groupId = 1000010;

        String receiverType = "group";

        String messageType = "image";
        String content = "test3";
        String path = "D:/IT/Project/MyChat/file/user/11111/dog.jpg";

        boolean isSendChatMessage = chatRepository.sendMessage(String.valueOf(userId), String.valueOf(receiverId), String.valueOf(groupId), receiverType, content, messageType, token, path);
        assertTrue(isSendChatMessage);
        System.out.println("发送消息成功");
    }

    @Test
    public void chatTest3() throws IOException {
        Integer userId = 11111;
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMSIsImlhdCI6MTczMjE2MzgyOSwiZXhwIjoxNzMyNzY4NjI5fQ.HE34u4UEsGf2lw7uq8e_9n279pUQK81AM8s9KaT2wkk";
        Integer receiverId = 0;
        Integer groupId = 1000010;

        List<ChatMessageModel> chatMessageModelList = chatRepository.getMessages(String.valueOf(userId), 0, token);
        System.out.println(chatMessageModelList);

        DownloadModel downloadModel = chatRepository.downloadFile(String.valueOf(userId), String.valueOf(chatMessageModelList.get(1).getMessageId()), token);
        System.out.println(downloadModel);
        System.out.println(downloadModel.getInputStream().available());

        try (InputStream is = downloadModel.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                System.out.println("读取了 " + bytesRead + " 字节数据");
                // 在这里处理数据，比如写入到本地文件
            }
            System.out.println("文件读取完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void messageConvertTest() {
        String message1 = "ServerMessage(senderId=11111, receiverId=11113, groupId=0, content=hahaha3, time=1732371614, messageType=text)";
        String message2 = "ServerMessage(senderId=0, receiverId=11113, groupId=0, content=11112:friend request get, time=1732371775, messageType=text)";

        // 测试解析
        ServerMessageModel model1 = ServerMessageMapper.mapToServerMessageModel(message1);
        ServerMessageModel model2 = ServerMessageMapper.mapToServerMessageModel(message2);

        Log.d("DBTest", model1.toString());
        Log.d("DBTest", model2.toString());

        boolean isServerMessage1 = ServerMessageMapper.isServerMessage(model1);
        boolean isServerMessage2 = ServerMessageMapper.isServerMessage(model2);
        ChatMessageModel chatMessageModel;
        if (isServerMessage1) {
            Log.d("DBTest", "Message 1 is a server message");
        } else {
            Log.d("DBTest", "Message 1 is not a server message");
            chatMessageModel = ServerMessageMapper.mapToChatMessageModel(model1);
        }
        if (isServerMessage2) {
            Log.d("DBTest", "Message 2 is a server message");
        } else {
            Log.d("DBTest", "Message 2 is not a server message");
        }
    }

    @Test
    public void HashTest() {
        String password = "12345asdasdwqewqeq!41/.?e67e7b273407d33637e1c7f178800a5dfe4130e2da1e714b03d16b552638c9be？";
        String passwordHash = HashGenerator.getPasswordHash(password);
        System.out.println(passwordHash);
        System.out.println(passwordHash.length());
    }

}