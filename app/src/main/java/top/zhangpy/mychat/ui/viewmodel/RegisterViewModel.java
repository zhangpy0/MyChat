package top.zhangpy.mychat.ui.viewmodel;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import lombok.Getter;
import top.zhangpy.mychat.data.remote.model.RequestMapModel;
import top.zhangpy.mychat.data.repository.UserRepository;
import top.zhangpy.mychat.util.HashGenerator;

@Getter
public class RegisterViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> userId = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> authCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCodeSent = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> showError = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final UserRepository userRepository;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void onUserIdChanged(int newUserId) {
        userId.setValue(newUserId);
    }

    // 更新邮箱
    public void onEmailChanged(String newEmail) {
        email.setValue(newEmail);
    }

    // 更新验证码
    public void onVerificationCodeChanged(String newCode) {
        authCode.setValue(newCode);
    }

    // 更新密码
    public void onPasswordChanged(String newPassword) {
        password.setValue(newPassword);
    }

    // 验证邮箱格式
    public boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void sendVerificationCode() {
        String emailValue = email.getValue();

        if (emailValue == null || !validateEmail(emailValue)) {
            errorMessage.setValue("请输入有效的邮箱地址");
            return;
        }

        // 开始发送验证码
        isLoading.setValue(true);
        errorMessage.setValue(null);

        Executors.newSingleThreadExecutor().execute(() -> {
            RequestMapModel requestMapModel = new RequestMapModel();
            requestMapModel.setEmail(emailValue);
            try {
                if (userRepository.sendEmailForRegister(requestMapModel)) {
                    isCodeSent.postValue(true);
                } else {
                    errorMessage.postValue("发送验证码失败");
                }
            } catch (Exception e) {
                errorMessage.postValue("发送验证码失败: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void register() {
        String userIdValue = String.valueOf(userId.getValue());
        String emailValue = email.getValue();
        String passwordValue = password.getValue();
        String authCodeValue = authCode.getValue();

        if (userId.getValue() == null || emailValue == null || passwordValue == null || authCodeValue == null) {
            showError.setValue(true);
            errorMessage.setValue("账号、邮箱、密码和验证码不能为空");
            return;
        }

        if (!validateEmail(emailValue)) {
            showError.setValue(true);
            errorMessage.setValue("请输入有效的邮箱地址");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        Executors.newSingleThreadExecutor().execute(() -> {
            RequestMapModel requestMapModel = new RequestMapModel();
            requestMapModel.setUserId(userIdValue);
            requestMapModel.setEmail(emailValue);
            requestMapModel.setPasswordHash(HashGenerator.getPasswordHash(passwordValue));
            requestMapModel.setAuthCode(authCodeValue);
            try {
                if (userRepository.register(requestMapModel)) {
                    errorMessage.postValue("注册成功");
                } else {
                    errorMessage.postValue("注册失败");
                }
            } catch (Exception e) {
                errorMessage.postValue("注册失败: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
