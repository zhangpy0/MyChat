package top.zhangpy.mychat.ui.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ChatListItem {

    private String contactName;
    private String senderName;
    private String content;
    private String time;
    private int unreadCount;
    private String avatarPath;
    private long absTime;

    public void resetContent() {
        if (content == null) {
            this.content = "";
        }
        if (content.length() > 16) {
            this.content = content.substring(0, 16) + "...";
        }
    }

    public void setTime(Timestamp sendTime) {
        if (sendTime == null) {
            this.time = "";
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        long diff = now.getTime() - sendTime.getTime();
        long oneDay = 24 * 60 * 60 * 1000;
        long oneYear = 365 * oneDay;

        if (diff < oneDay) {
            this.time = new java.text.SimpleDateFormat("HH:mm").format(sendTime);
        } else if (diff < 2 * oneDay) {
            this.time = "昨天";
        } else if (diff < oneYear) {
            this.time = new java.text.SimpleDateFormat("MM-dd").format(sendTime);
        } else {
            this.time = new java.text.SimpleDateFormat("yyyy-MM").format(sendTime);
        }
    }
}
