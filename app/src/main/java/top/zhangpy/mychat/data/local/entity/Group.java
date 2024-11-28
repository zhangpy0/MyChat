package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

import lombok.Data;

@Data
@Entity(tableName = "groups")
public class Group {

    @PrimaryKey
    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @ColumnInfo(name = "creator_id")
    private Integer creatorId;

    @ColumnInfo(name = "created_at")
    private Timestamp createdAt;

    @ColumnInfo(name = "message_time")
    private Timestamp messageTime;

}
