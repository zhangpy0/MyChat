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
import top.zhangpy.mychat.data.remote.model.GroupInfoModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;

public interface GroupService {

    /**
     * 修改群聊信息
     *
     * @param token 用户Token
     * @param body  包含用户ID、群聊ID、群聊名称、群公告的请求体
     * @return 响应结果
     */
    @POST("group/updateGroupInfo")
    Call<ResultModel> updateGroupInfo(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 修改群聊头像
     *
     * @param token  用户Token
     * @param userId 用户ID
     * @param groupId 群聊ID
     * @param avatar 群聊头像文件
     * @return 响应结果
     */
    @Multipart
    @POST("group/updateGroupAvatar")
    Call<ResultModel> updateGroupAvatar(
            @Header("token") String token,
            @Query("userId") String userId,
            @Query("groupId") String groupId,
            @Part MultipartBody.Part avatar
    );

    /**
     * 获取群聊信息
     *
     * @param token 用户Token
     * @param body  包含用户ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("group/getGroupInfo")
    Call<ResultModel<GroupInfoModel>> getGroupInfo(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 解散群聊
     *
     * @param token 用户Token
     * @param body  包含用户ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("group/deleteGroup")
    Call<ResultModel> deleteGroup(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 创建群聊
     *
     * @param token 用户Token
     * @param body  包含创建人ID的请求体
     * @return 响应结果
     */
    @POST("group/createGroup")
    Call<ResultModel> createGroup(
            @Header("token") String token,
            @Body Map<String, String> body
    );
}
