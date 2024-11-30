package top.zhangpy.mychat.ui.model;

import lombok.Data;

@Data
public class ApplyListItem {

    private int id;
    private String contactName;
    private String senderName;
    private String content;
    private int  flag; // 0: received invite, 1: sent apply, 2: received apply

    private String type; // "friend" or "group"
    private String status; // "未处理" or "已添加" or "已拒绝"
    private String avatarPath;
    private long absTime;

    public void reset() {
        if (type.equals("group")) {
            if (flag == 0) {
                content = senderName + "邀请你加入群聊: " + content;
            } else if (flag == 1) {
                content = "申请加入群聊: " + content;
            } else if (flag == 2) {
                content = senderName + "申请加入群聊: " + content;
            }
        } else {
            if (flag == 0 || flag == 2) {
                content = senderName + "请求添加你为好友: " + content;
            } else if (flag == 1) {
                content = "请求添加" + contactName + "为好友: " + content;
            }
        }
        switch (status) {
            case "pending":
                status = "未处理";
                break;
            case "approved":
                status = "已添加";
                break;
            case "rejected":
                status = "已拒绝";
                break;
        }
    }
}
