package top.zhangpy.mychat.data.remote.api;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import top.zhangpy.mychat.data.remote.model.ChatMessageModel;
import top.zhangpy.mychat.data.remote.model.ResultModel;

public interface ChatService {

    /**
     * 获取消息
     *
     * @param userId 用户ID
     * @param time   Unix时间戳（秒）
     * @param token  用户Token
     * @return 包含消息的响应
     */
    @POST("chat/update")
    Call<ResultModel<List<ChatMessageModel>>> getMessages(
            @Query("userId") String userId,
            @Query("time") long time,
            @Header("token") String token
    );

    /**
     * 发送消息
     *
     * @param userId       用户ID
     * @param receiverId   接收者ID（可选）
     * @param groupId      群组ID（可选）
     * @param receiverType 接收者类型
     * @param content      消息内容（可选）
     * @param messageType  消息类型
     * @param token        用户Token
     * @param file         消息文件（可选）
     * @return 发送消息的响应
     */
    @Multipart
    @POST("chat/send")
    Call<ResultModel> sendMessage(
            @Query("userId") String userId,
            @Query("receiverId") String receiverId,
            @Query("groupId") String groupId,
            @Query("receiverType") String receiverType,
            @Query("content") String content,
            @Query("messageType") String messageType,
            @Header("token") String token,
            @Part MultipartBody.Part file
    );

    /**
     * 下载文件
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @param token     用户Token
     * @return 包含文件数据的响应
     */
    @Streaming
    @POST("chat/download")
    Call<ResponseBody> downloadFile(
            @Query("userId") String userId,
            @Query("messageId") String messageId,
            @Header("token") String token
    );
}

