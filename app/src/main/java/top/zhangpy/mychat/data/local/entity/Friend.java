package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.Objects;

import lombok.Data;

@Data
@Entity(tableName = "friends", indices = {@Index(value = {"user_id", "friend_id"}, unique = true)})
public class Friend {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "user_id")
    private Integer userId;

    @ColumnInfo(name = "friend_id")
    private Integer friendId;

    @ColumnInfo(name = "created_at")
    private Timestamp createdAt;

    @ColumnInfo(name = "status")
    private String status; // 0 user cant send to friend, 1 user can send to friend

    @ColumnInfo(name = "message_time")
    private Timestamp messageTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend that)) return false;
        return Objects.equals(userId, that.getUserId()) && Objects.equals(friendId, that.getFriendId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId);
    }
}
