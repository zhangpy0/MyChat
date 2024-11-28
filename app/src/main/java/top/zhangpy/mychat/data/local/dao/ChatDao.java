package top.zhangpy.mychat.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import top.zhangpy.mychat.data.local.entity.ChatMessage;

@Dao
public interface ChatDao {

    @Insert
    public void insertChatMessage(ChatMessage chatMessage);

    /**
     * Update chat message (only isDownload,isRead field)
     * @param chatMessage
     */
    @Update
    public void updateChatMessage(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_messages ORDER BY send_time DESC")
    public List<ChatMessage> getAllChatMessagesSortedBySendTime();

    @Delete
    public void deleteChatMessage(ChatMessage chatMessage);
}
