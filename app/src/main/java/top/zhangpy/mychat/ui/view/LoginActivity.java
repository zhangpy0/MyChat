package top.zhangpy.mychat.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.LoginViewModel;


// TODO 1. 检查数据库自动登录 2. user 创建后 userprofile 创建  DONE
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_user);
        
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Bind UI elements
        EditText userIdOrEmailEditText = findViewById(R.id.log_id);
        EditText passwordEditText = findViewById(R.id.log_password);
        Button loginButton = findViewById(R.id.btn_login);
        ImageView backButton = findViewById(R.id.close);

        // Observe LiveData for errors
        loginViewModel.getShowError().observe(this, showError -> {
            if (showError != null && showError) {
                showToast(loginViewModel.getErrorMessage().getValue());
            }
        });

        // Set login button click listener
        loginButton.setOnClickListener(v -> {
            String userIdOrEmail = userIdOrEmailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Update ViewModel data
            loginViewModel.getUserIdOrEmail().setValue(userIdOrEmail);
            loginViewModel.getPassword().setValue(password);

            // Perform login action
            loginViewModel.login();

            Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
            startActivity(intent);
            finish();
        });

        // Set back button click listener
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
