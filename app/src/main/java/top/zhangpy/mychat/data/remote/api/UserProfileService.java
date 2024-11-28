package top.zhangpy.mychat.data.remote.api;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.data.remote.model.UserAvatarModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;

public interface UserProfileService {

    /**
     * 修改用户信息(地区)
     *
     * @param token 用户Token
     * @param body  包含用户ID和新地区的请求体
     * @return 响应结果
     */
    @POST("profile/updateUserRegion")
    Call<ResultModel> updateUserRegion(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 修改用户信息(昵称)
     *
     * @param token 用户Token
     * @param body  包含用户ID和新昵称的请求体
     * @return 响应结果
     */
    @POST("profile/updateUserNickname")
    Call<ResultModel> updateUserNickname(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 修改用户信息(性别)
     *
     * @param token 用户Token
     * @param body  包含用户ID和新性别的请求体
     * @return 响应结果
     */
    @POST("profile/updateUserGender")
    Call<ResultModel> updateUserGender(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 修改用户信息(头像)
     *
     * @param token  用户Token
     * @param userId 用户ID
     * @param avatar 用户头像文件
     * @return 响应结果
     */
    @Multipart
    @POST("profile/updateUserAvatar")
    Call<ResultModel> updateUserAvatar(
            @Header("token") String token,
            @Query("userId") String userId,
            @Part MultipartBody.Part avatar
    );

    /**
     * 获取用户信息
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("profile/getUserProfile")
    Call<ResultModel<UserProfileModel>> getUserProfile(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取用户信息(头像)
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("profile/getUserAvatar")
    Call<ResultModel<UserAvatarModel>> getUserAvatar(
            @Header("token") String token,
            @Body Map<String, String> body
    );
}
