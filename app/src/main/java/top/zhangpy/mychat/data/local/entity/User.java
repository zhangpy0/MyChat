package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private Integer userId;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "token")
    private String token;
}
