package top.zhangpy.mychat.data.remote.model;

import java.util.HashMap;
import java.util.Map;


public class RequestMapModel {

    private String userId;
    private String email;
    private String passwordHash;
    private String authCode;
    private String newPasswordHash;
    private String region;
    private String gender;
    private String nickname;
    private String groupId;
    private String groupName;
    private String announcement;
    private String friendId;
    private String message;
    private String status;
    private String otherId;

    public RequestMapModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getNewPasswordHash() {
        return newPasswordHash;
    }

    public void setNewPasswordHash(String newPasswordHash) {
        this.newPasswordHash = newPasswordHash;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        if (userId != null) {
            map.put("userId", userId);
        }
        if (email != null) {
            map.put("email", email);
        }
        if (passwordHash != null) {
            map.put("passwordHash", passwordHash);
        }
        if (authCode != null) {
            map.put("authCode", authCode);
        }
        if (newPasswordHash != null) {
            map.put("newPasswordHash", newPasswordHash);
        }
        if (region != null) {
            map.put("region", region);
        }
        if (gender != null) {
            map.put("gender", gender);
        }
        if (nickname != null) {
            map.put("nickname", nickname);
        }
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (groupName != null) {
            map.put("groupName", groupName);
        }
        if (announcement != null) {
            map.put("announcement", announcement);
        }
        if (friendId != null) {
            map.put("friendId", friendId);
        }
        if (message != null) {
            map.put("message", message);
        }
        if (status != null) {
            map.put("status", status);
        }
        if (otherId != null) {
            map.put("otherId", otherId);
        }

        return map;
    }
}
