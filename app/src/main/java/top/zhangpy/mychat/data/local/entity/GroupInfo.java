package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "group_info")
public class GroupInfo {

    @PrimaryKey
    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @ColumnInfo(name = "group_name")
    private String groupName;

    @ColumnInfo(name = "announcement")
    private String announcement;

    @ColumnInfo(name = "avatar_path")
    private String avatarPath;


}
