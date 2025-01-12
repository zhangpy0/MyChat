package top.zhangpy.mychat.ui.model;

import java.sql.Timestamp;
import java.util.Calendar;

import lombok.Data;

@Data
public class ChatListItem {

    private int id;
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

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 今日0点
        Timestamp today = new Timestamp(calendar.getTimeInMillis());
        long diff = today.getTime() - sendTime.getTime();
        long oneDay = 24 * 60 * 60 * 1000;
        long oneYear = 365 * oneDay;

        if (diff <= 0) {
            this.time = new java.text.SimpleDateFormat("HH:mm").format(sendTime);
        } else if (diff < 1 * oneDay) {
            this.time = "昨天";
        } else if (diff < oneYear) {
            this.time = new java.text.SimpleDateFormat("MM-dd").format(sendTime);
        } else {
            this.time = new java.text.SimpleDateFormat("yyyy-MM").format(sendTime);
        }
    }
}
