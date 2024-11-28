package top.zhangpy.mychat.data.remote.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import top.zhangpy.mychat.data.remote.model.ContactApplyModel;
import top.zhangpy.mychat.data.remote.model.FriendModel;
import top.zhangpy.mychat.data.remote.model.GroupInfoModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;
import top.zhangpy.mychat.data.remote.model.UserProfileModel;

public interface ContactService {

    /**
     * 更新好友状态
     *
     * @param token 用户Token
     * @param body  包含用户ID、好友ID和状态的请求体
     * @return 响应结果
     */
    @POST("contact/updateFriendStatus")
    Call<ResultModel> updateFriendStatus(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 申请加入群聊
     *
     * @param token 用户Token
     * @param body  包含用户ID、群聊ID和申请信息的请求体
     * @return 响应结果
     */
    @POST("contact/sendGroupRequest")
    Call<ResultModel> sendGroupRequest(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 发起好友申请
     *
     * @param token 用户Token
     * @param body  包含用户ID、好友ID和申请信息的请求体
     * @return 响应结果
     */
    @POST("contact/sendFriendRequest")
    Call<ResultModel> sendFriendRequest(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 搜索用户
     *
     * @param token 用户Token
     * @param body  包含用户ID和好友ID的请求体
     * @return 响应结果
     */
    @POST("contact/searchUser")
    Call<ResultModel<UserProfileModel>> searchUser(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 搜索群聊
     *
     * @param token 用户Token
     * @param body  包含用户ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("contact/searchGroup")
    Call<ResultModel<GroupInfoModel>> searchGroup(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 处理群聊申请
     *
     * @param token 用户Token
     * @param body  包含用户ID、发起人ID、群聊ID和状态的请求体
     * @return 响应结果
     */
    @POST("contact/processGroupRequest")
    Call<ResultModel> processGroupRequest(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 处理好友申请
     *
     * @param token 用户Token
     * @param body  包含用户ID、好友ID和状态的请求体
     * @return 响应结果
     */
    @POST("contact/processFriendRequest")
    Call<ResultModel> processFriendRequest(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取群列表
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("contact/getGroups")
    Call<ResultModel<List<Map<String, String>>>> getGroups(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取群主
     *
     * @param token 用户Token
     * @param body  包含用户ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("contact/getGroupOwner")
    Call<ResultModel<Map<String, String>>> getGroupOwner(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取群成员
     *
     * @param token 用户Token
     * @param body  包含用户ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("contact/getGroupMembers")
    Call<ResultModel<List<Map<String, String>>>> getGroupMembers(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取好友列表
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("contact/getFriends")
    Call<ResultModel<List<FriendModel>>> getFriends(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取好友申请(入群申请)
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("contact/getContactApplyFromOthers")
    Call<ResultModel<List<ContactApplyModel>>> getContactApplyFromOthers(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 获取自己发出的好友申请(入群申请)
     *
     * @param token 用户Token
     * @param body  包含用户ID的请求体
     * @return 响应结果
     */
    @POST("contact/getContactApplyFromMe")
    Call<ResultModel<List<ContactApplyModel>>> getContactApplyFromMe(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 删除好友
     *
     * @param token 用户Token
     * @param body  包含用户ID和好友ID的请求体
     * @return 响应结果
     */
    @POST("contact/deleteFriend")
    Call<ResultModel> deleteFriend(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 踢出群成员
     *
     * @param token 用户Token
     * @param body  包含用户ID、好友ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("contact/deleteFriendFromGroup")
    Call<ResultModel> deleteFriendFromGroup(
            @Header("token") String token,
            @Body Map<String, String> body
    );

    /**
     * 添加好友入群
     *
     * @param token 用户Token
     * @param body  包含用户ID、好友ID和群聊ID的请求体
     * @return 响应结果
     */
    @POST("contact/addFriendToGroup")
    Call<ResultModel> addFriendToGroup(
            @Header("token") String token,
            @Body Map<String, String> body
    );
}

