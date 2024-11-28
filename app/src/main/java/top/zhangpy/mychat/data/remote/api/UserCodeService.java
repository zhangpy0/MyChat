package top.zhangpy.mychat.data.remote.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import top.zhangpy.mychat.data.remote.model.ResultModel;

public interface UserCodeService {

    /**
     * 注册时获取验证码
     *
     * @param email 邮箱地址的 JSON 对象
     * @return 包含验证码结果的响应
     */
    @POST("sendEmailForRegister")
    Call<ResultModel> sendEmailForRegister(@Body Map<String, String> email);

    /**
     * 修改密码时获取验证码
     *
     * @param email 邮箱地址的 JSON 对象
     * @return 包含验证码结果的响应
     */
    @POST("sendEmailForChangePassword")
    Call<ResultModel> sendEmailForChangePassword(@Body Map<String, String> email);
}
