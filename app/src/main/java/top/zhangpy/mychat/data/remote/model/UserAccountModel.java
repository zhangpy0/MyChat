package top.zhangpy.mychat.data.remote.model;

import lombok.Data;

// /login data
@Data
public class UserAccountModel {

    private String userId;
    private String email;
    private String passwordHash;
    private String token;

}
