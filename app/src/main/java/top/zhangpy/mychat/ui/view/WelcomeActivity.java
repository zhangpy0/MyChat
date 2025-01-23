package top.zhangpy.mychat.ui.view;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.concurrent.Executors;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.data.local.entity.User;
import top.zhangpy.mychat.data.mapper.UserAccountMapper;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.data.service.MessageService;


public class WelcomeActivity extends Activity {

    private UserRepository userRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome); //设置布局\
        userRepository = new UserRepository(this);
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isExist = false;
            List<User> users = null;
            try {
                users = userRepository.getAllUsers();
                if (users.size() == 1) {
                    isExist = true;
                    Integer userId = users.get(0).getUserId();
                    String token = users.get(0).getToken();
                    String passwordHash = users.get(0).getPasswordHash();
                    RequestMapModel requestMapModel = new RequestMapModel();
                    requestMapModel.setUserId(userId.toString());
                    requestMapModel.setPasswordHash(passwordHash);
                    UserAccountModel userAccountModel = userRepository.login(requestMapModel);
                    if (userAccountModel != null) {
                        User user = UserAccountMapper.mapToUser(userAccountModel);
                        userRepository.updateUser(user);
                        saveIdToken(this, userId, user.getToken());

                        runServices();

                        Intent intent = new Intent(WelcomeActivity.this, ChatListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            } catch (Exception e) {
                Log.e("WelcomeActivity", e.getMessage());
            } finally {
                if (!isExist) {
                    Log.i("WelcomeActivity", "No user found");
                } else {
                    Log.i("WelcomeActivity", "User found");
                    User locaUser = users.get(0);
                    saveIdToken(this, locaUser.getUserId(), locaUser.getToken());

                    runServices();
                    Intent intent = new Intent(WelcomeActivity.this, ChatListActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
    }

    //登录按钮点击事件处理方法
    public void welcome_login(View v) {

        Intent intent = new Intent();
        /*页面跳转到登录界面*/
        intent.setClass(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
//        this.finish(); //结束当前activity
    }

    //注册按钮点击事件处理方法
    public void welcome_register(View v) {
        Intent intent = new Intent();
        /*页面跳转到注册界面*/
        intent.setClass(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
//        this.finish(); //结束当前activity
    }

    private void saveIdToken(Context context, Integer userId , String token) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("user_id", userId).apply();
        prefs.edit().putString("auth_token", token).apply();
        prefs.edit().apply();
    }

    private void runServices() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isMessageServiceRunning = false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MessageService.class.getName().equals(service.service.getClassName())) {
                isMessageServiceRunning = true;
            }
        }
        if (!isMessageServiceRunning) {
            Intent serviceReceiverIntent = new Intent(this.getApplication(), MessageService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication().getApplicationContext().startForegroundService(serviceReceiverIntent); // Android 8.0+ 启动前台服务
            } else {
                getApplication().getApplicationContext().startService(serviceReceiverIntent); // 低版本直接启动服务
            }
        }
    }
}
