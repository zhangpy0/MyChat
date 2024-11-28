package top.zhangpy.mychat.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.Objects;

import lombok.Data;

@Data
@Entity(tableName = "contact_apply")
public class ContactApply {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "apply_id")
    private Integer applyId;

    @ColumnInfo(name = "applicant_id")
    private Integer applicantId;

    @ColumnInfo(name = "contact_type")
    private String contactType;

    @ColumnInfo(name = "receiver_id")
    private Integer receiverId;

    @ColumnInfo(name = "group_id")
    private Integer groupId;

    @ColumnInfo(name = "apply_time")
    private Timestamp applyTime;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "message")
    private String message;

    public enum ContactType {
        FRIEND, GROUP
    }

    public enum ApplyStatus {
        PENDING, APPROVED, REJECTED
    }

    public void nullToEmpty() {
        if (this.message == null) {
            this.message = "";
        }
        if (this.groupId == null) {
            this.groupId = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactApply that)) return false;
        return getApplicantId().equals(that.getApplicantId()) &&
                getReceiverId().equals(that.getReceiverId()) &&
                getGroupId().equals(that.getGroupId()) &&
                getApplyTime().equals(that.getApplyTime()) &&
                getMessage().equals(that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicantId, receiverId, groupId, applyTime, message);
    }

}
