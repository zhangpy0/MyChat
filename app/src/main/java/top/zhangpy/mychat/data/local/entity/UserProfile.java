package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "user_profiles", indices = {@Index(value = {"user_id"}, unique = true)})
public class UserProfile {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    private Integer profileId;

    @ColumnInfo(name = "user_id")
    private Integer userId;

    @ColumnInfo(name = "avatar_path")
    private String avatarPath;

    @ColumnInfo(name = "nickname")
    private String nickname;

    @ColumnInfo(name = "gender")
    private String gender;

    @ColumnInfo(name = "region")
    private String region;

    public void nullToEmpty() {
        if (avatarPath == null) {
            avatarPath = "";
        }
        if (nickname == null) {
            nickname = "";
        }
        if (gender == null) {
            gender = "";
        }
        if (region == null) {
            region = "";
        }
    }
}

