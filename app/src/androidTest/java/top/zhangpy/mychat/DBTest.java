package top.zhangpy.mychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import top.zhangpy.mychat.data.local.entity.ChatMessage;
import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.local.entity.UserProfile;
import top.zhangpy.mychat.data.mapper.ChatMessageMapper;
import top.zhangpy.mychat.data.mapper.ServerMessageMapper;
import top.zhangpy.mychat.data.mapper.UserAccountMapper;
import top.zhangpy.mychat.data.mapper.UserProfileMapper;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.ServerMessageModel;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;
import top.zhangpy.mychat.data.repository.ChatRepository;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.data.service.MessageReceiverService;
import top.zhangpy.mychat.util.Logger;
import top.zhangpy.mychat.util.StorageHelper;

@RunWith(AndroidJUnit4.class)
public class DBTest {

    private UserRepository userRepository;

    private ChatRepository chatRepository;

    private Context context;


    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        this.context = context;
        Logger.d("DBTest", context.getDatabasePath("main_database").toString());
        Logger.d("DBTest", context.getFilesDir().getAbsolutePath());
        userRepository = new UserRepository(context);

        chatRepository = new ChatRepository(context);
    }

    @After
    public void tearDown() {
        userRepository = null;
        chatRepository = null;
    }

    @Test
    public void storageTest() {
        String path = "content://com.tencent.mtt.fileprovider/external_file_path/extract_root/QB_extracted/%E8%AE%BA%E6%96%87%E8%B5%84%E6%96%99/%E8%AE%BA%E6%96%87%E8%B5%84%E6%96%99/%E6%95%B0%E5%AD%97%E5%8C%96%E8%BD%AC%E5%9E%8B/%E4%BA%A7%E5%93%81%E6%9C%8D%E5%8A%A1%E4%BE%9B%E5%BA%94%E9%93%BE%E5%AE%9A%E4%BB%B7%E5%86%B3%E7%AD%96%EF%BC%9A%E6%95%B0...%E6%BA%90%E6%8C%96%E6%8E%98%E4%B8%8E%E5%85%B1%E4%BA%AB%E7%AD%96%E7%95%A5%E7%9A%84%E5%BD%B1%E5%93%8D%E5%88%86%E6%9E%90_%E5%88%98%E4%B8%9C%E9%9C%9E.pdf";
        String realPath = StorageHelper.getRealPathFromURI(context, path);
        Logger.d("Test", "Real path: " + realPath);
    }

    @Test
    public void userTest() throws IOException {
        Integer userId = 11113;
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMTExMyIsImlhdCI6MTczMjEwMjQ0NiwiZXhwIjoxNzMyNzA3MjQ2fQ.USt5psOImqFF49o2ANaYuMtx6OTiNRMpKm8pnV1QKjs";
        String email = "1304152517@qq.com";
        String passwordHash = "12345";

        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        requestMapModel.setPasswordHash(passwordHash);

        List<User> users = userRepository.getAllUsers();
        if (!users.isEmpty()) {
            Logger.d("DBTest", users.toString());
        }

        UserAccountModel userAccountModel = userRepository.login(requestMapModel);
        Logger.d("DBTest", userAccountModel.toString());


        User user = UserAccountMapper.mapToUser(userAccountModel);

        userRepository.insertUser(user);
        Logger.d("DBTest", users.toString());
        users = userRepository.getAllUsers();
        Logger.d("DBTest", users.toString());

        String token = user.getToken();
        saveToken(token);

        requestMapModel.setFriendId(String.valueOf(userId));

        UserProfileModel userProfileModel = userRepository.getUserProfile(token, requestMapModel);
        Logger.d("DBTest", userProfileModel.toString());
        UserProfile userProfile = UserProfileMapper.mapToUserProfile(userProfileModel, context);
        userRepository.insertUserProfile(userProfile);
        UserProfile userProfile1 = userRepository.getUserProfileById(userId);
        Logger.d("DBTest", userProfile1.toString());
        userProfile1.setNickname("丁真");
        userRepository.updateUserProfile(userProfile1);
        userProfile1 = userRepository.getUserProfileById(userId);
        Logger.d("DBTest", userProfile1.toString());
        userRepository.deleteUserProfile(userProfile1);
        userProfile1 = userRepository.getUserProfileById(userId);
        assert userProfile1 == null;

        Intent serviceIntent = new Intent(context, MessageReceiverService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // 注册广播接收器
        CountDownLatch latch = new CountDownLatch(1);

        IntentFilter filter = new IntentFilter("top.zhangpy.mychat.MESSAGE_RECEIVED");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                Logger.d("DBTest", "Received broadcast message: " + message);
                latch.countDown(); // 收到消息后结束等待
            }
        };
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);


        try {
            if (!latch.await(100, TimeUnit.SECONDS)) {
                Logger.e("DBTest", "Timeout waiting for service message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 清理
        context.unregisterReceiver(receiver);
        context.stopService(serviceIntent);

        Logger.d("DBTest", "Test completed");
    }
    private void saveToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();
    }

    @Test
    public void DBTest2() throws IOException {
        Integer userId = 11113;
        String email = "1304152517@qq.com";
        String passwordHash = "12345";

        RequestMapModel requestMapModel = new RequestMapModel();
        requestMapModel.setUserId(String.valueOf(userId));
        requestMapModel.setPasswordHash(passwordHash);

        final String[] token = {null};

        UserAccountModel userAccountModel = userRepository.login(requestMapModel);
        Logger.d("DBTest", userAccountModel.toString());
        User user = UserAccountMapper.mapToUser(userAccountModel);
        userRepository.insertUser(user);
        token[0] = user.getToken();
        saveToken(token[0]);

        String message1 = "ServerMessage(senderId=11111, receiverId=11113, groupId=0, content=5:dog.jpg, time=1732371614, messageType=image)";
        String message2 = "ServerMessage(senderId=0, receiverId=11113, groupId=0, content=11112:friend request get, time=1732371775, messageType=text)";

        // 测试解析
        ServerMessageModel model1 = ServerMessageMapper.mapToServerMessageModel(message1);
        ServerMessageModel model2 = ServerMessageMapper.mapToServerMessageModel(message2);

        Logger.d("DBTest", model1.toString());
        Logger.d("DBTest", model2.toString());

        boolean isServerMessage1 = ServerMessageMapper.isServerMessage(model1);
        boolean isServerMessage2 = ServerMessageMapper.isServerMessage(model2);
        ChatMessageModel chatMessageModel = null;
        if (isServerMessage1) {
            Logger.d("DBTest", "Message 1 is a server message");
        } else {
            Logger.d("DBTest", "Message 1 is not a server message");
            chatMessageModel = ServerMessageMapper.mapToChatMessageModel(model1);
        }
        if (isServerMessage2) {
            Logger.d("DBTest", "Message 2 is a server message");
        } else {
            Logger.d("DBTest", "Message 2 is not a server message");
        }

        assert chatMessageModel != null;
        ChatMessage chatMessage = ChatMessageMapper.mapToChatMessage(chatMessageModel);
        Logger.d("DBTest", chatMessage.toString());

        chatRepository.createChatTable(ChatRepository.getTableName(chatMessage));
        Logger.d("DBTest", "Table created : " + ChatRepository.getTableName(chatMessage));
        chatRepository.insertMessage(ChatRepository.getTableName(chatMessage), chatMessage);
        List<ChatMessage> messages = chatRepository.getMessages(ChatRepository.getTableName(chatMessage));
        Logger.d("DBTest", messages.toString());

        chatMessage = messages.get(0);

        String path = chatRepository.downloadFile(context, String.valueOf(userId), token[0], chatMessage);
        Logger.d("DBTest", "File downloaded to: " + path);

        messages = chatRepository.getMessages(ChatRepository.getTableName(chatMessage));
        Logger.d("DBTest", messages.toString());
        path = chatRepository.downloadFile(context, String.valueOf(userId), token[0], chatMessage);
        Logger.d("DBTest", "File downloaded to: " + path);
        Logger.d("DBTest", "Test completed");
    }
}
