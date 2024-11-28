package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "group_members", indices = {@Index(value = {"group_id", "user_id"}, unique = true)})
public class GroupMember {

    @PrimaryKey
    private Integer id;

    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @ColumnInfo(name = "user_id")
    private Integer userId;

    @ColumnInfo(name = "role")
    private String role;
}
