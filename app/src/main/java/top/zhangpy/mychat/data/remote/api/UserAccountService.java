package top.zhangpy.mychat.data.remote.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.data.remote.model.UserAccountModel;

public interface UserAccountService {

    /**
     * 注册用户
     *
     * @param userDetails 包含邮箱(email)、验证码(authCode)、用户ID(userId)、密码哈希(passwordHash)的 JSON 对象
     * @return 包含注册结果的响应
     */
    @POST("register")
    Call<ResultModel> register(@Body Map<String, String> userDetails);

    /**
     * 用户登录
     *
     * @param loginDetails 包含用户ID/邮箱(userId 或 email)和密码哈希(passwordHash)的 JSON 对象
     * @return 包含登录结果的响应
     */
    @POST("login")
    Call<ResultModel<UserAccountModel>> login(@Body Map<String, String> loginDetails);

    /**
     * 修改密码
     *
     * @param passwordDetails 包含邮箱(email)、验证码(authCode)和新密码哈希(newPasswordHash)的 JSON 对象
     * @return 包含修改密码结果的响应
     */
    @POST("changePassword")
    Call<ResultModel> changePassword(@Body Map<String, String> passwordDetails);

    @POST("check")
    Call<ResultModel> check(
            @Header("token") String token,
            @Body Map<String, String> checkDetails
    );
}

