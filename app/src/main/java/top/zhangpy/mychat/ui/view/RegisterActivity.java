package top.zhangpy.mychat.ui.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.zhangpy.mychat.R;
import top.zhangpy.mychat.ui.viewmodel.RegisterViewModel;
import top.zhangpy.mychat.util.StringJudge;

public class RegisterActivity extends AppCompatActivity {
    private RegisterViewModel registerViewModel;

    private EditText etAccountId;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etVerificationCode;
    private Button btnSendCode;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);

        // 初始化 ViewModel
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // 绑定视图
        ImageView backButton = findViewById(R.id.iv_register_back);
        TextView tvTitle = findViewById(R.id.tv_register_title);
        etAccountId = findViewById(R.id.reg_id);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etVerificationCode = findViewById(R.id.et_verification_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnRegister = findViewById(R.id.btn_register);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 发送验证码按钮点击事件
        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            if (TextUtils.isEmpty(email)) {
                showToast("邮箱不能为空");
                return;
            }

            // 更新 ViewModel 数据并调用发送验证码方法
            registerViewModel.onEmailChanged(email);
            registerViewModel.sendVerificationCode();
        });

        // 注册按钮点击事件
        btnRegister.setOnClickListener(v -> {
            String accountId = etAccountId.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String verificationCode = etVerificationCode.getText().toString();

            if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(verificationCode)) {
                showToast("请填写所有必填字段");
                return;
            }

            if (!StringJudge.isIdLegal(accountId)) {
                showToast("账号长度应在5-10个字符之间，且为数字");
                return;
            }

            if (!StringJudge.isEmail(email)) {
                showToast("邮箱格式不正确");
                return;
            }

            if (!StringJudge.isInRange(password)) {
                showToast("密码长度应在5-20个字符之间");
                return;
            }

            // 更新 ViewModel 数据
            registerViewModel.onUserIdChanged(Integer.parseInt(accountId));
            registerViewModel.onEmailChanged(email);
            registerViewModel.onPasswordChanged(password);
            registerViewModel.onVerificationCodeChanged(verificationCode);

            // 调用注册方法
            registerViewModel.register();
        });

        // 监听 LiveData 数据
        observeViewModel();
    }

    private void observeViewModel() {
        // 监听验证码发送状态
        registerViewModel.getIsCodeSent().observe(this, isCodeSent -> {
            if (isCodeSent != null && isCodeSent) {
                btnSendCode.setText("已发送");
                btnSendCode.setEnabled(false);
            }
        });

        // 监听加载状态
        registerViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                btnRegister.setEnabled(false);
                btnSendCode.setEnabled(false);
            } else {
                btnRegister.setEnabled(true);
                btnSendCode.setEnabled(true);
            }
        });

        // 监听错误信息
        registerViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                showToast(errorMessage);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
